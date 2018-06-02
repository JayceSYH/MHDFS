package com.syh.mdfs.nodeserver.Service;

import Config.DataConfig;
import Config.NameServerConfig;
import Config.NodeServerConfig;
import Config.ProtoConfig;
import Exceptions.DataBlockNotAvailable;
import Exceptions.DataPersistException;
import Util.Str2MD5;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.syh.mdfs.nodeserver.Model.PersistentFileBlockMeta;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class StdBlockManager implements BlockManager {
    private Map<String, Map<Integer, PersistentFileBlockMeta>> blockMapping;
    private Set<String> fileHashSet;
    private ReentrantReadWriteLock fileWriteLock = new ReentrantReadWriteLock();
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private StdBlockManager() {
        // initiate block mapping
        this.blockMapping = new HashMap<>();
        this.fileHashSet = new HashSet<>();
        loadBlockMapping();
    }

    @Override
    public Map<String, Map<Integer, PersistentFileBlockMeta>> getFileBlockMapping() {
        return this.blockMapping;
    }

    @Override
    public void saveBlock(PersistentFileBlockMeta blockMeta, byte[] content) throws DataPersistException {
        String hashCode = Str2MD5.MD5(blockMeta.getAbsPath() + ProtoConfig.pathSep + blockMeta.getBlockNo());
        readWriteLock.readLock().lock();
        while (fileHashSet.contains(hashCode)) {
            hashCode = Str2MD5.MD5(hashCode + "elf");
        }
        readWriteLock.readLock().unlock();

        boolean dataPersisted = persistBlockData(hashCode, content);
        if (!dataPersisted)
            throw new DataPersistException();

        readWriteLock.writeLock().lock();
        fileHashSet.add(hashCode);
        blockMeta.setBlockHashCode(hashCode);
        if (!blockMapping.containsKey(blockMeta.getAbsPath()))
            blockMapping.put(blockMeta.getAbsPath(), new HashMap<>());
        blockMapping.get(blockMeta.getAbsPath()).put(blockMeta.getBlockNo(), blockMeta);
        readWriteLock.writeLock().unlock();
        persistBlockMapping();
    }

    @Override
    public byte[] getBlock(String absPath, int noBlock) throws DataBlockNotAvailable {
        readWriteLock.readLock().lock();
        if (!blockMapping.containsKey(absPath) || !blockMapping.get(absPath).containsKey(noBlock))
            throw new DataBlockNotAvailable();

        PersistentFileBlockMeta persistentFileBlockMeta = blockMapping.get(absPath).get(noBlock);
        readWriteLock.readLock().unlock();
        byte[] content = fetchBlockData(persistentFileBlockMeta.getBlockHashCode(), persistentFileBlockMeta.getBlockSize());

        if (content == null)
            throw new DataBlockNotAvailable();

        return content;
    }

    @Override
    public void deleteBlock(String filePath, int blockNo) {
        String hashCode = null;
        readWriteLock.writeLock().lock();
        try {
            if (blockMapping.containsKey(filePath) && blockMapping.get(filePath).containsKey(blockNo)) {
                hashCode = blockMapping.get(filePath).get(blockNo).getBlockHashCode();
                fileHashSet.remove(hashCode);
                blockMapping.get(filePath).remove(blockNo);
                if (blockMapping.get(filePath).size() == 0)
                    blockMapping.remove(filePath);
            }
        }
        finally {
            readWriteLock.writeLock().unlock();
        }

        if (hashCode != null)
            deleteBlockData(hashCode);
        persistBlockMapping();
    }

    @Override
    public void deleteFileBlocks(String filePath) {
        if (blockMapping.containsKey(filePath)) {
            readWriteLock.writeLock().lock();
            Map<Integer, PersistentFileBlockMeta> blockMetaMap = blockMapping.get(filePath);
            blockMapping.remove(filePath);
            for (int blockNo : blockMetaMap.keySet()) {
                PersistentFileBlockMeta blockMeta = blockMetaMap.get(blockNo);
                fileHashSet.remove(blockMeta.getBlockHashCode());
            }
            readWriteLock.writeLock().unlock();

            persistBlockMapping();

            for (int blockNo : blockMetaMap.keySet()) {
                PersistentFileBlockMeta blockMeta = blockMetaMap.get(blockNo);
                deleteBlockData(blockMeta.getBlockHashCode());
            }
        }
    }

    @Override
    public Map<String, Map<Integer, String>> getLoadInfo() {
        Map<String, Map<Integer, String>> loadInfo = new HashMap<>();
        for (String fileName : blockMapping.keySet()) {
            loadInfo.put(fileName, new HashMap<>());
            for (int blockNo : blockMapping.get(fileName).keySet()) {
                loadInfo.get(fileName).put(blockNo, blockMapping.get(fileName).get(blockNo).getBlockHashCode());
            }
        }
        return loadInfo;
    }

    private boolean persistBlockData(String hashCode, byte[] content) {
        File blockDir = new File(DataConfig.blockSavingDir);
        if (!blockDir.exists())
            blockDir.mkdir();

        File blockFile = new File(DataConfig.blockSavingDir + ProtoConfig.pathSep + hashCode);
        try {
            blockFile.createNewFile();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        try {
            try (FileOutputStream outputStream = new FileOutputStream(blockFile)) {
                outputStream.write(content);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void deleteBlockData(String hashCode) {
        File blockFile = new File(DataConfig.blockSavingDir + ProtoConfig.pathSep + hashCode);
        if (blockFile.exists())
            blockFile.delete();
    }

    private byte[] fetchBlockData(String hashCode, int blockSize) {
        File blockFile = new File(DataConfig.blockSavingDir + ProtoConfig.pathSep + hashCode);
        try {
            FileInputStream inputStream = new FileInputStream(blockFile);
            byte[] content = new byte[blockSize];
            int readBytes = inputStream.read(content);

            if (readBytes < blockSize)
                return null;
            else
                return content;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void persistBlockMapping() {
        this.fileWriteLock.writeLock().lock();
        try {
            this.readWriteLock.readLock().lock();
            String jsonStr = JSON.toJSONString(this.blockMapping);
            this.readWriteLock.readLock().unlock();

            File file = new File(NodeServerConfig.blockMappingPersistFile);
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

    private void loadBlockMapping() {
        File file = new File(NodeServerConfig.blockMappingPersistFile);
        if (file.exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(file);
                byte[] content = new byte[(int)file.length()];
                inputStream.read(content);
                inputStream.close();
                String jsonStr = new String(content);
                this.blockMapping = JSON.parseObject(jsonStr, new TypeReference<Map<String, Map<Integer, PersistentFileBlockMeta>>>(){});

                for (String fileName : blockMapping.keySet()) {
                    Map<Integer, PersistentFileBlockMeta> blocks = blockMapping.get(fileName);
                    for (Integer i : blocks.keySet())
                        fileHashSet.add(blocks.get(i).getBlockHashCode());
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
