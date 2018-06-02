package com.syh.mdfs.nameserver.Services;

import Exceptions.ConnectionFailedException;
import Exceptions.DataBlockNotAvailable;
import Exceptions.DataPersistException;
import FileStructure.BaseFileBlockMeta;
import FileStructure.FileBlockMeta;
import Util.HttpPost;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.syh.mdfs.nameserver.Models.MDFSFileBlockMeta;
import com.syh.mdfs.nameserver.Models.MDFSFileMetaData;
import com.syh.mdfs.nameserver.Models.MDFSFileNode;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StdMessageSender implements MessageSender{

    @Override
    public Map<String, Map<Integer, BaseFileBlockMeta>> fetchFileBlockMapping(MDFSFileNode fileNode) {
        String jsonStr = HttpPost.formUploadRetString("http://" + fileNode.getIp() + ":" + fileNode.getPort() +
                "/blockmapping", new HashMap<>(), new HashMap<>());
        Map<String, Map<Integer, BaseFileBlockMeta>> blockMapping = JSON.parseObject(jsonStr, new TypeReference<Map<String, Map<Integer, BaseFileBlockMeta>>>(){});
        fileNode.resetBlockMapping(blockMapping);
        return blockMapping;
    }

    @Override
    public void saveBlockToFileNode(MDFSFileNode fileNode, MDFSFileBlockMeta blockMeta, byte[] content) throws DataPersistException {
        String status = HttpPost.formUploadRetString("http://" + fileNode.getIp() + ":" + fileNode.getPort() + "/block/save",
                new HashMap<String, String>(){{put("blockMeta", JSON.toJSONString(blockMeta));}},
                new HashMap<String, byte[]>(){{put("content", content);}});
        if (!status.equals("ok"))
            throw new DataPersistException();
        blockMeta.addAvailableFileNodes(fileNode);
    }

    @Override
    public byte[] downloadBlockFromFileNode(MDFSFileNode fileNode, MDFSFileBlockMeta blockMeta) throws DataBlockNotAvailable {
        byte[] content = HttpPost.formUploadRetBytes("http://" + fileNode.getIp() + ":" + fileNode.getPort() + "/block/get",
                new HashMap<String, String>(){{put("blockMeta", JSON.toJSONString(blockMeta));}},
                new HashMap<>());
        if (content.length == 0)
            throw new DataBlockNotAvailable();
        else
            return content;
    }

    @Override
    public void deleteBlockFromFileNode(MDFSFileNode fileNode, MDFSFileBlockMeta blockMeta) throws ConnectionFailedException {
        String status = HttpPost.formUploadRetString("http://" + fileNode.getIp() + ":" + fileNode.getPort() + "/block/delete",
                new HashMap<String, String>(){{put("filePath", blockMeta.getAbsPath());
                put("blockNo", String.valueOf(blockMeta.getBlockNo()));}},
                new HashMap<String, byte[]>());
        if (!status.equals("ok"))
            throw new ConnectionFailedException();
        blockMeta.deleteAvailableFileNodes(fileNode);
    }

    @Override
    public void deleteFileBlocksFromFileNode(MDFSFileNode fileNode, MDFSFileMetaData fileMetaData) throws ConnectionFailedException {
        String status = HttpPost.formUploadRetString("http://" + fileNode.getIp() + ":" + fileNode.getPort() + "/block/delete",
                new HashMap<String, String>(){{put("filePath", fileMetaData.getAbsPath());}},
                new HashMap<String, byte[]>());
        if (!status.equals("ok"))
            throw new ConnectionFailedException();
        fileMetaData.deprecateAllNodes();
    }
}
