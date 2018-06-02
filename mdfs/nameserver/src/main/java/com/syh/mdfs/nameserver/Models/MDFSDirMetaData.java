package com.syh.mdfs.nameserver.Models;

import Config.ProtoConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MDFSDirMetaData implements MDFSObjectMetaData {
    private String dirName;
    private String absPath;
    private String parentDir;
    private Map<String, MDFSObjectMetaData> dirContents;

    public MDFSDirMetaData(String dirName, String parentDir, Map<String, MDFSObjectMetaData> dirContents) {
        this.dirName = dirName;
        this.absPath = parentDir + dirName + ProtoConfig.pathSep;
        this.dirContents = dirContents;
        this.parentDir = parentDir;
    }

    public MDFSDirMetaData(String dirName, String parentDir) {
        this.dirName = dirName;
        this.parentDir = parentDir;
        this.absPath = parentDir + dirName + ProtoConfig.pathSep;
        this.dirContents = new HashMap<>();
    }


    @Override
    public boolean isDir() {
        return true;
    }

    @Override
    public String getPath() {
        return this.absPath;
    }

    @Override
    public String getName() {
        return this.dirName;
    }

    @Override
    public int getSize() {
        int dirSize = 0;
        for (String fileName : this.dirContents.keySet())
            dirSize += this.dirContents.get(fileName).getSize();
        return dirSize;
    }

    @Override
    public MDFSFileMetaData getFileMetaData() {
        return null;
    }

    @Override
    public MDFSDirMetaData getDirMetaData() {
        return this;
    }

    public List<MDFSObjectMetaData> listDir() {
        List<MDFSObjectMetaData> retList = new ArrayList<>();
        for (String key : this.dirContents.keySet()) {
            retList.add(this.dirContents.get(key));
        }

        return retList;
    }

    public void addDirContent(MDFSObjectMetaData obj) {
        this.dirContents.put(obj.getName(), obj);
    }

    public void removeDirContent(MDFSObjectMetaData obj) {
        this.dirContents.remove(obj.getName());
    }

    public String getDirName() {
        return dirName;
    }

    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    public String getAbsPath() {
        return absPath;
    }

    public void setAbsPath(String absPath) {
        this.absPath = absPath;
    }

    public String getParentDir() {
        return parentDir;
    }

    public void setParentDir(String parentDir) {
        this.parentDir = parentDir;
    }

    public Map<String, MDFSObjectMetaData> getDirContents() {
        return dirContents;
    }

    public void setDirContents(Map<String, MDFSObjectMetaData> dirContents) {
        this.dirContents = dirContents;
    }
}
