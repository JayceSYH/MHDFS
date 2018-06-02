package com.syh.mdfs.nameserver.Models;

import FileStructure.BaseFileBlockMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MDFSFileBlockMeta extends BaseFileBlockMeta {

    private Map<String, MDFSFileNode> MDFSFileNodes = new HashMap<>();

    public MDFSFileBlockMeta(String fileName, String absPath, int blockSize, int totalBlockNum, int blockNo) {
        super(fileName, absPath, blockSize, totalBlockNum, blockNo);
    }

    public List<MDFSFileNode> getAvailableFileNodes() {
        List<MDFSFileNode> retList = new ArrayList<>();
        for (String id : this.MDFSFileNodes.keySet()) {
            retList.add(this.MDFSFileNodes.get(id));
        }
        return retList;
    }

    public void addAvailableFileNodes(MDFSFileNode fileNode) {
        this.MDFSFileNodes.put(fileNode.getInstanceId(), fileNode);
        fileNode.addBlock(this);
    }

    public void deleteAvailableFileNodes(MDFSFileNode fileNode) {
        this.MDFSFileNodes.remove(fileNode.getInstanceId());
        fileNode.removeBlock(this);
    }

    public boolean hasCopyOnFileNode(MDFSFileNode fileNode) {
        return this.MDFSFileNodes.keySet().contains(fileNode.getInstanceId());
    }

    public void resetAvailableFileNodes(Map<String, MDFSFileNode> nodes) {
        this.MDFSFileNodes = nodes;
    }
}
