package com.syh.mdfs.nameserver.Models;

import java.util.ArrayList;
import java.util.List;

public class EasyToSaveObjectMeta {
    private boolean isDir;
    private String name;
    private String parentDir;
    private int size;
    private int totalBlockNum;
    private int blockSize;
    private List<String> dirContents = new ArrayList<>();

    public EasyToSaveObjectMeta(MDFSObjectMetaData objectMetaData) {
        this.name = objectMetaData.getName();
        this.parentDir = objectMetaData.getParentDir();
        this.size = objectMetaData.getSize();


        if (objectMetaData.isDir()) {
            MDFSDirMetaData dirMetaData = objectMetaData.getDirMetaData();
            this.isDir = true;
            for (MDFSObjectMetaData objectMeta : dirMetaData.listDir()) {
                dirContents.add(objectMeta.getPath());
            }
        }
        else {
            MDFSFileMetaData fileMetaData = objectMetaData.getFileMetaData();
            totalBlockNum = fileMetaData.getTotalBlockNum();
            blockSize = fileMetaData.getBlockSize();
        }
    }

    public EasyToSaveObjectMeta() {

    }

    public MDFSFileMetaData toFileMetaData() {
        return new MDFSFileMetaData(this.name, this.parentDir, this.size, this.blockSize, this.totalBlockNum);
    }

    public MDFSDirMetaData toDirMetaData() {
        return new MDFSDirMetaData(this.name, this.parentDir);
    }


    public List<String> getDirContents() {
        return dirContents;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotalBlockNum() {
        return totalBlockNum;
    }

    public void setTotalBlockNum(int totalBlockNum) {
        this.totalBlockNum = totalBlockNum;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public void setDirContents(List<String> dirContents) {
        this.dirContents = dirContents;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }

    public String getParentDir() {
        return parentDir;
    }

    public void setParentDir(String parentDir) {
        this.parentDir = parentDir;
    }
}
