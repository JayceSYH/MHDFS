package com.syh.mdfs.nodeserver.Service;

import Exceptions.DataBlockNotAvailable;
import Exceptions.DataPersistException;
import com.syh.mdfs.nodeserver.Model.PersistentFileBlockMeta;

import java.util.Map;

public interface BlockManager {
    Map<String, Map<Integer, PersistentFileBlockMeta>> getFileBlockMapping();
    void saveBlock(PersistentFileBlockMeta blockMeta, byte[] content) throws DataPersistException;
    byte[] getBlock(String absPath, int noBlock) throws DataBlockNotAvailable;
    void deleteBlock(String filePath, int blockNo);
    void deleteFileBlocks(String filePath);
    Map<String, Map<Integer, String>> getLoadInfo();
}
