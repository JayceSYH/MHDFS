package com.syh.mdfs.nameserver.Services;

import Exceptions.FileBlockNotAvailableException;
import Exceptions.InvalidParamsException;
import Exceptions.NodeNotAvailableException;
import com.syh.mdfs.nameserver.Models.MDFSFileNode;
import com.syh.mdfs.nameserver.Models.MDFSObjectMetaData;

import java.util.List;

public interface FileManager {
    void resetFileMapping(MDFSFileNode MDFSFileNode);
    void uploadFile(String dirPath, String fileName, byte[] content) throws NodeNotAvailableException, InvalidParamsException;
    void createDir(String dirPath, String subDirName) throws InvalidParamsException;
    byte[] downloadFile(String filePath) throws FileBlockNotAvailableException, InvalidParamsException;
    void removeObj(String path);
    void notifyNodeDeprecated(MDFSFileNode fileNode);
    MDFSObjectMetaData getObjMetaData(String objPath);
}
