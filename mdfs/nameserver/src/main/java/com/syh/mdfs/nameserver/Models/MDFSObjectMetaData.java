package com.syh.mdfs.nameserver.Models;

public interface MDFSObjectMetaData {
    // is file or directory
    boolean isDir();

    // get path
    String getPath();

    // get parent directory
    String getParentDir();

    // get name
    String getName();

    // get obj size
    int getSize(); // in KB

    // get File Meta Data
    MDFSFileMetaData getFileMetaData();

    // get Dir Meta Data
    MDFSDirMetaData getDirMetaData();


}
