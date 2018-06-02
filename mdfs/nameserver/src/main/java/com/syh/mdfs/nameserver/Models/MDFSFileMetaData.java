package com.syh.mdfs.nameserver.Models;

import Config.ProtoConfig;
import FileStructure.BaseFileBlockMeta;
import FileStructure.FileBlockMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MDFSFileMetaData implements MDFSObjectMetaData {
    private String fileName;
    private String parentDir;
    private String absPath;
    private int fileSize;
    private int totalBlockNum;
    private int blockSize;
    private List<MDFSFileBlockMeta> blocks;
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public MDFSFileMetaData(String fileName, String parentDir, int fileSize, int blockSize, int totalBlockNum) {
        this.fileSize = fileSize;
        this.totalBlockNum = totalBlockNum;
        this.fileName = fileName;
        this.blockSize = blockSize;
        this.parentDir = parentDir;
        this.absPath = parentDir + fileName;

        this.blocks = new ArrayList<>();
        for (int i = 0; i < totalBlockNum; i++) {
            if (i < totalBlockNum - 1)
                this.blocks.add(new MDFSFileBlockMeta(fileName, absPath, blockSize, totalBlockNum, i));
            else
                this.blocks.add(new MDFSFileBlockMeta(fileName, absPath, fileSize - blockSize * i, totalBlockNum, i));
        }
    }

    @Override
    public boolean isDir() {
        return false;
    }

    @Override
    public String getPath() {
        return this.absPath;
    }

    @Override
    public String getName() {
        return this.fileName;
    }

    @Override
    public int getSize() {
        return this.fileSize;
    }

    @Override
    public MDFSFileMetaData getFileMetaData() {
        return this;
    }

    @Override
    public MDFSDirMetaData getDirMetaData() {
        return null;
    }

    public void updateBlockMapping(Map<Integer, BaseFileBlockMeta> blockMetaMap, MDFSFileNode fileNode) {
        readWriteLock.writeLock().lock();
        for (int no : blockMetaMap.keySet()) {
            blocks.get(no).addAvailableFileNodes(fileNode);
        }
        readWriteLock.writeLock().unlock();
    }

    public void deprecateAllNodes() {
        readWriteLock.writeLock().lock();
        for (MDFSFileBlockMeta blockMeta : this.blocks) {
            blockMeta.resetAvailableFileNodes(new HashMap<>());
        }
        readWriteLock.writeLock().unlock();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setBlocks(List<MDFSFileBlockMeta> blocks) {
        readWriteLock.writeLock().lock();
        this.blocks = blocks;
        readWriteLock.writeLock().unlock();
    }

    public void resetBlock(MDFSFileBlockMeta block) {
        this.blocks.set(block.getBlockNo(), block);
    }

    public MDFSFileBlockMeta getBlockByNo(int no) {
        readWriteLock.readLock().lock();
        try {
            return this.blocks.get(no);
        }
        finally {
            readWriteLock.readLock().unlock();
        }
    }

    public int getTotalBlockNum() {
        return totalBlockNum;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public String getParentDir() {
        return parentDir;
    }

    public void setParentDir(String parentDir) {
        this.parentDir = parentDir;
    }

    public String getAbsPath() {
        return absPath;
    }

    public void setAbsPath(String absPath) {
        this.absPath = absPath;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public void setTotalBlockNum(int totalBlockNum) {
        this.totalBlockNum = totalBlockNum;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public List<MDFSFileBlockMeta> getBlocks() {
        return blocks;
    }

    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    public void setReadWriteLock(ReadWriteLock readWriteLock) {
        this.readWriteLock = readWriteLock;
    }
}
