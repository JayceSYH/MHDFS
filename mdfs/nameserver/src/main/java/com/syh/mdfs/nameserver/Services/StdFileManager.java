package com.syh.mdfs.nameserver.Services;

import Config.DataConfig;
import Config.NameServerConfig;
import Config.NodeServerConfig;
import Config.ProtoConfig;
import Exceptions.*;
import FileStructure.BaseFileBlockMeta;
import Util.BytesCombiner;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.syh.mdfs.nameserver.Models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.Integer.min;


@Service
public class StdFileManager implements FileManager, Runnable {

    @Autowired
    private NodeManager nodeManager;
    @Autowired
    private MessageSender messageSender;
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock fileWriteLock = new ReentrantReadWriteLock();

    private Map<String, MDFSObjectMetaData> fileMapping = new HashMap<>();

    private StdFileManager() {
        loadFileMapping();
        new Thread(this).start();
    }

    @Override
    public void resetFileMapping(MDFSFileNode fileNode) {
        Map<String, Map<Integer, BaseFileBlockMeta>> blockMapping = messageSender.fetchFileBlockMapping(fileNode);
        fileNode.resetBlockMapping(blockMapping);
        readWriteLock.writeLock().lock();
        for (String filePath : blockMapping.keySet()) {
            if (fileMapping.containsKey(filePath)) {
                MDFSFileMetaData fileMetaData = fileMapping.get(filePath).getFileMetaData();
                fileMetaData.updateBlockMapping(blockMapping.get(filePath), fileNode);
            }
        }
        readWriteLock.writeLock().unlock();
        persistFileMapping();
    }

    @Override
    public void uploadFile(String dirPath, String fileName, byte[] content) throws NodeNotAvailableException, InvalidParamsException {

        MDFSObjectMetaData objMeta = getObjMeta(dirPath);
        if (objMeta == null || !objMeta.isDir())
            throw new InvalidParamsException();

        String fileAbsPath = dirPath + fileName;
        if (getObjMeta(fileAbsPath) != null)
            return;

        int fileSize = content.length;
        int blockSizeInBytes = DataConfig.blockSize * 1024;
        int blockNum = (fileSize + (blockSizeInBytes - 1)) / blockSizeInBytes;
        MDFSFileMetaData fileMetaData = new MDFSFileMetaData(fileName, dirPath, fileSize, blockSizeInBytes, blockNum);

        List<MDFSFileNode> fileNodes = nodeManager.getAvailableNodesByLoad();
        int blocksToSavePerCycle = DataConfig.minBlocksPerSavingProcess;
        for (int i = 0; i < blockNum; ++i) {
            int toSaveCopies = DataConfig.backupNum + 1;
            for (MDFSFileNode fileNode : fileNodes) {
                try {
                    messageSender.saveBlockToFileNode(fileNode, fileMetaData.getBlockByNo(i),
                            Arrays.copyOfRange(content, i * blockSizeInBytes, min((i + 1) * blockSizeInBytes, fileSize)));
                    toSaveCopies -= 1;
                    blocksToSavePerCycle -= 1;
                    if (toSaveCopies == 0)
                        break;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (blocksToSavePerCycle <= 0) {
                blocksToSavePerCycle = DataConfig.minBlocksPerSavingProcess;
                fileNodes = nodeManager.getAvailableNodesByLoad();
            }

            if (toSaveCopies == DataConfig.backupNum + 1)
                throw new NodeNotAvailableException();
        }

        putObjMeta(dirPath, fileAbsPath, fileMetaData);
    }

    @Override
    public void createDir(String dirPath, String subDirName) throws InvalidParamsException {
        MDFSObjectMetaData objMeta = getObjMeta(dirPath);
        if (objMeta == null || !objMeta.isDir())
            throw new InvalidParamsException();

        String absPath = dirPath + subDirName + ProtoConfig.pathSep;
        if (isObjMetaExists(absPath))
            throw new InvalidParamsException();

        MDFSDirMetaData dirMetaData = new MDFSDirMetaData(subDirName, dirPath);
        putObjMeta(dirPath, absPath, dirMetaData);
    }

    @Override
    public byte[] downloadFile(String filePath) throws InvalidParamsException, FileBlockNotAvailableException {
        MDFSObjectMetaData objMeta = getObjMeta(filePath);
        if (objMeta == null || objMeta.isDir())
            throw  new InvalidParamsException();

        MDFSFileMetaData fileMetaData = objMeta.getFileMetaData();
        List<byte[]> contentParts = new ArrayList<>();
        for (int i = 0; i < fileMetaData.getTotalBlockNum(); i++) {
            MDFSFileBlockMeta blockMeta = fileMetaData.getBlockByNo(i);
            List<MDFSFileNode> fileNodes = blockMeta.getAvailableFileNodes();
            for (MDFSFileNode fileNode : fileNodes) {
                try {
                    byte[] content = messageSender.downloadBlockFromFileNode(fileNode, blockMeta);
                    contentParts.add(content);
                    break;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (contentParts.size() <= i)
                throw new FileBlockNotAvailableException();
        }

        return BytesCombiner.concatContentParts(contentParts);
    }

    @Override
    public void removeObj(String path) {
        MDFSObjectMetaData objectMetaData = fileMapping.get(path);
        removeObjMeta(objectMetaData);
        if (!objectMetaData.isDir()) {
            for (MDFSFileBlockMeta blockMeta : objectMetaData.getFileMetaData().getBlocks()) {
                for (MDFSFileNode fileNode : blockMeta.getAvailableFileNodes()) {
                    try {
                        messageSender.deleteBlockFromFileNode(fileNode, blockMeta);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        persistFileMapping();
    }

    @Override
    public void notifyNodeDeprecated(MDFSFileNode fileNode) {
        readWriteLock.writeLock().lock();
        try {
            List<String> nodeFiles = new ArrayList<>(fileNode.getBlockMapping().keySet());
            for (String fileName : nodeFiles) {
                if (this.fileMapping.containsKey(fileName) && !this.fileMapping.get(fileName).isDir()) {
                    Map<Integer, BaseFileBlockMeta> blockMapping = fileNode.getBlockMapping().get(fileName);
                    List<Integer> blockNoList = new ArrayList<>(blockMapping.keySet());
                    for (int blockNo : blockNoList) {
                        this.fileMapping.get(fileName).getFileMetaData().getBlockByNo(blockNo).deleteAvailableFileNodes(fileNode);
                    }
                }
            }
        }
        finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public MDFSObjectMetaData getObjMetaData(String objPath) {
        readWriteLock.readLock().lock();
        try {
            return fileMapping.getOrDefault(objPath, null);
        }
        finally {
            readWriteLock.readLock().unlock();
        }
    }

    private void putObjMeta(String dirPath, String filePath, MDFSObjectMetaData meta) {
        this.readWriteLock.writeLock().lock();
        this.fileMapping.get(dirPath).getDirMetaData().addDirContent(meta);
        this.fileMapping.put(filePath, meta);
        this.readWriteLock.writeLock().unlock();
        persistFileMapping();
    }

    private void removeObjMeta(MDFSObjectMetaData objectMetaData) {
        readWriteLock.writeLock().lock();
        recursivelyRemoveObjMetaWithoutLock(objectMetaData);
        readWriteLock.writeLock().unlock();
    }

    private void recursivelyRemoveObjMetaWithoutLock(MDFSObjectMetaData objectMetaData) {
        fileMapping.get(objectMetaData.getParentDir()).getDirMetaData().removeDirContent(objectMetaData);
        if (objectMetaData.isDir()) {
            MDFSDirMetaData dirMetaData = objectMetaData.getDirMetaData();
            for (MDFSObjectMetaData subObjMeta : dirMetaData.listDir())
                recursivelyRemoveObjMetaWithoutLock(subObjMeta);
        }
        fileMapping.remove(objectMetaData.getPath());
    }

    private MDFSObjectMetaData getObjMeta(String absPath) {
        this.readWriteLock.readLock().lock();
        try {
            return this.fileMapping.get(absPath);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        finally {
            this.readWriteLock.readLock().unlock();
        }
    }

    private boolean isObjMetaExists(String absPath) {
        this.readWriteLock.readLock().lock();
        try {
            return this.fileMapping.containsKey(absPath);
        }
        finally {
            this.readWriteLock.readLock().unlock();
        }
    }

    private void persistFileMapping() {
        this.fileWriteLock.writeLock().lock();
        try {
            this.readWriteLock.readLock().lock();
            Map<String, EasyToSaveObjectMeta> saveMap = new HashMap<>();
            for (String fileName : fileMapping.keySet()) {
                EasyToSaveObjectMeta saveObj = new EasyToSaveObjectMeta(fileMapping.get(fileName));
                saveMap.put(fileName, saveObj);
            }
            this.readWriteLock.readLock().unlock();
            String jsonStr = JSON.toJSONString(saveMap);

            File file = new File(NameServerConfig.fileMappingPersistFile);
            try {
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(jsonStr.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        finally {
            this.fileWriteLock.writeLock().unlock();
        }
    }

    private void loadFileMapping() {
        File file = new File(NameServerConfig.fileMappingPersistFile);
        if (file.exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(file);
                byte[] content = new byte[(int)file.length()];
                inputStream.read(content);
                inputStream.close();
                String jsonStr = new String(content);
                Map<String, EasyToSaveObjectMeta> saveMap = JSON.parseObject(jsonStr, new TypeReference<Map<String, EasyToSaveObjectMeta>>(){});
                for (String fileName : saveMap.keySet()) {
                    if (saveMap.get(fileName).isDir())
                        this.fileMapping.put(fileName, saveMap.get(fileName).toDirMetaData());
                    else
                        this.fileMapping.put(fileName, saveMap.get(fileName).toFileMetaData());
                }

                for (String fileName : fileMapping.keySet()) {
                    if (fileMapping.get(fileName).isDir()) {
                        List<String> dirContents = saveMap.get(fileName).getDirContents();
                        for (String subFile : dirContents) {
                            fileMapping.get(fileName).getDirMetaData().addDirContent(fileMapping.get(subFile));
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                fileMapping = new HashMap<>();
                fileMapping.put("/", new MDFSDirMetaData("/", "/"));
            }
        }
        else {
            fileMapping = new HashMap<>();
            fileMapping.put("/", new MDFSDirMetaData("/", "/"));
        }
    }

    private byte[] downloadBlock(MDFSFileBlockMeta blockMeta) {
        byte[] content = null;

        for (MDFSFileNode fileNode : blockMeta.getAvailableFileNodes()) {
            try {
                content = messageSender.downloadBlockFromFileNode(fileNode, blockMeta);
                break;
            }
            catch (DataBlockNotAvailable e) {
                e.printStackTrace();
                content = null;
            }
        }

        return content;
    }

    private void balancedUploadBlocksWithoutLock(Map<String, Map<Integer, Integer>> toBackupBlocks) {
        List<MDFSFileNode> availableNodes = nodeManager.getAvailableNodesByLoad();
        int toSaveBlocksPerCycle = DataConfig.minBlocksPerSavingProcess;

        for (String fileName : toBackupBlocks.keySet()) {
            for (int blockNo : toBackupBlocks.get(fileName).keySet()) {
                if (toSaveBlocksPerCycle <= 0)
                    availableNodes = nodeManager.getAvailableNodesByLoad();

                MDFSFileBlockMeta blockMeta = fileMapping.get(fileName).getFileMetaData().getBlockByNo(blockNo);
                byte[] content = downloadBlock(blockMeta);
                int toSaveCopies = toBackupBlocks.get(fileName).get(blockNo);
                for (MDFSFileNode fileNode : availableNodes) {
                    if (blockMeta.hasCopyOnFileNode(fileNode))
                        continue;

                    try {
                        messageSender.saveBlockToFileNode(fileNode, blockMeta, content);
                        toSaveCopies -= 1;
                        toSaveBlocksPerCycle -= 1;
                        if (toSaveCopies == 0)
                            break;
                    }
                    catch (DataPersistException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    @Override
    public void run() {
        try {
            Thread.sleep(NodeServerConfig.backupInitWaitTime);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                Map<String, Map<Integer, Integer>> toBackupBlocks = new HashMap<>();
                readWriteLock.readLock().lock();
                try {
                    for (String fileName : this.fileMapping.keySet()) {
                        MDFSObjectMetaData objectMetaData = this.fileMapping.get(fileName);
                        if (!objectMetaData.isDir()) {
                            MDFSFileMetaData fileMetaData = objectMetaData.getFileMetaData();
                            toBackupBlocks.put(fileName, new HashMap<>());
                            for (MDFSFileBlockMeta blockMeta : fileMetaData.getBlocks()) {
                                if (blockMeta.getAvailableFileNodes().size() < DataConfig.backupNum + 1)
                                    toBackupBlocks.get(fileName).put(blockMeta.getBlockNo(), DataConfig.backupNum + 1 - blockMeta.getAvailableFileNodes().size());
                            }
                            if (toBackupBlocks.get(fileName).size() == 0)
                                toBackupBlocks.remove(fileName);
                        }
                    }

                    balancedUploadBlocksWithoutLock(toBackupBlocks);
                }
                finally {
                    readWriteLock.readLock().unlock();
                }

                try {
                    Thread.sleep(NodeServerConfig.backupCheckCycle);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
