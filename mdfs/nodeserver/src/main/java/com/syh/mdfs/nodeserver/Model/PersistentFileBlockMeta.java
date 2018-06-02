package com.syh.mdfs.nodeserver.Model;

import FileStructure.BaseFileBlockMeta;

public class PersistentFileBlockMeta extends BaseFileBlockMeta {

    private String blockHashCode;

    public PersistentFileBlockMeta() {

    }

    public PersistentFileBlockMeta(String fileName, String absPath, int blockSize, int totalBlockNum, int blockNo) {
        super(fileName, absPath, blockSize, totalBlockNum, blockNo);
    }

    public String getBlockHashCode() {
        return blockHashCode;
    }

    public void setBlockHashCode(String blockHashCode) {
        this.blockHashCode = blockHashCode;
    }
}
