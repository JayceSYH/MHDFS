package com.syh.mdfs.nameserver.Services;

import Exceptions.ConnectionFailedException;
import Exceptions.DataBlockNotAvailable;
import Exceptions.DataPersistException;
import FileStructure.BaseFileBlockMeta;
import FileStructure.FileBlockMeta;
import com.syh.mdfs.nameserver.Models.MDFSFileBlockMeta;
import com.syh.mdfs.nameserver.Models.MDFSFileMetaData;
import com.syh.mdfs.nameserver.Models.MDFSFileNode;

import java.util.List;
import java.util.Map;

public interface MessageSender {
    Map<String, Map<Integer, BaseFileBlockMeta>> fetchFileBlockMapping(MDFSFileNode fileNode);
    void saveBlockToFileNode(MDFSFileNode fileNode, MDFSFileBlockMeta blockMeta, byte[] content) throws DataPersistException;
    byte[] downloadBlockFromFileNode(MDFSFileNode fileNode, MDFSFileBlockMeta blockMeta) throws DataBlockNotAvailable;
    void deleteBlockFromFileNode(MDFSFileNode fileNode, MDFSFileBlockMeta blockMeta)throws ConnectionFailedException;
    void deleteFileBlocksFromFileNode(MDFSFileNode fileNode, MDFSFileMetaData fileMetaData) throws ConnectionFailedException;
}
