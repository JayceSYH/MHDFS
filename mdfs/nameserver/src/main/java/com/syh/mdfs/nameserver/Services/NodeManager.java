package com.syh.mdfs.nameserver.Services;

import com.syh.mdfs.nameserver.Models.MDFSFileNode;

import java.util.List;

public interface NodeManager {
    public List<MDFSFileNode> getAvailableNodesByLoad();
}
