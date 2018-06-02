package com.syh.mdfs.nameserver.Models;


import Exceptions.DataBlockNotAvailable;
import Exceptions.DataPersistException;
import Exceptions.MDFSInternalException;
import Exceptions.NodeNotAvailableException;
import FileStructure.BaseFileBlockMeta;
import FileStructure.FileBlockMeta;
import com.syh.mdfs.nameserver.Services.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MDFSFileNode {

    private String ip;
    private int port;
    private String instanceId;
    private long upTimeStamp;
    private Map<String, Map<Integer, BaseFileBlockMeta>> blockMapping = new HashMap<>();

    public MDFSFileNode(String ip, int port, String instanceId, long upTimeStamp) {
        this.ip = ip;
        this.port = port;
        this.instanceId = instanceId;
        this.upTimeStamp = upTimeStamp;
    }

    public boolean checkRestartByUpTime(long upTimeStamp) {
        return upTimeStamp == this.upTimeStamp;
    }

    public void resetBlockMapping(Map<String, Map<Integer, BaseFileBlockMeta>> blockMapping) {
        this.blockMapping = blockMapping;
    }

    public void addBlock(MDFSFileBlockMeta blockMeta) {
        if (!this.blockMapping.containsKey(blockMeta.getAbsPath()))
            this.blockMapping.put(blockMeta.getAbsPath(), new HashMap<>());

        this.blockMapping.get(blockMeta.getAbsPath()).put(blockMeta.getBlockNo(), blockMeta);
    }

    public void removeBlock(MDFSFileBlockMeta blockMeta) {
        if (this.blockMapping.containsKey(blockMeta.getAbsPath()) && this.blockMapping.get(blockMeta.getAbsPath()).containsKey(blockMeta.getBlockNo())) {
            this.blockMapping.get(blockMeta.getAbsPath()).remove(blockMeta.getBlockNo());
            if (this.blockMapping.get(blockMeta.getAbsPath()).size() == 0)
                this.blockMapping.remove(blockMeta.getAbsPath());
        }
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public long getUpTimeStamp() {
        return upTimeStamp;
    }

    public Map<String, Map<Integer, BaseFileBlockMeta>> getBlockMapping() {
        return blockMapping;
    }

    public int getBlockCount() {
        int blockCount = 0;
        for (String fileName : blockMapping.keySet())
            blockCount += blockMapping.get(fileName).size();

        return blockCount;
    }
}
