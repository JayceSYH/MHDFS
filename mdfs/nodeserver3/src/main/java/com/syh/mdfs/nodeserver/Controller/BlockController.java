package com.syh.mdfs.nodeserver.Controller;

import FileStructure.BaseFileBlockMeta;
import com.alibaba.fastjson.JSON;
import com.sun.org.apache.regexp.internal.RE;
import com.syh.mdfs.nodeserver.Model.PersistentFileBlockMeta;
import com.syh.mdfs.nodeserver.Service.BlockManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
public class BlockController {

    @Autowired
    BlockManager blockManager;

    @RequestMapping(value = "/blockmapping", method = RequestMethod.POST)
    public String fetchBlockMapping() {
        return JSON.toJSONString(blockManager.getFileBlockMapping());
    }

    @RequestMapping(value = "/block/save", method = RequestMethod.POST)
    public String saveBlock(@RequestParam("content") MultipartFile multipartFile, @RequestParam("blockMeta") String blockMetaStr) {
        try {
            byte[] content = multipartFile.getBytes();
            PersistentFileBlockMeta blockMeta = JSON.parseObject(blockMetaStr, PersistentFileBlockMeta.class);
            blockManager.saveBlock(blockMeta, content);
            return "ok";
        }
        catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    @RequestMapping(value = "/block/get", method = RequestMethod.POST)
    public byte[] fetchBlock(@RequestParam("blockMeta") String strBlockMeta) {
        try {
            BaseFileBlockMeta blockMeta = JSON.parseObject(strBlockMeta, BaseFileBlockMeta.class);
            return blockManager.getBlock(blockMeta.getAbsPath(), blockMeta.getBlockNo());
        }
        catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    @RequestMapping(value = "/block/delete", method = RequestMethod.POST)
    public String deleteBlock(@RequestParam("filePath") String filePath, @RequestParam("blockNo") int blockNo) {
        blockManager.deleteBlock(filePath, blockNo);
        return "ok";
    }

    @RequestMapping(value = "/file/delete", method = RequestMethod.POST)
    public String deleteBlock(@RequestParam("filePath") String filePath) {
        blockManager.deleteFileBlocks(filePath);
        return "ok";
    }

    @RequestMapping(value = "/load", method = RequestMethod.GET)
    public String showLoad() {
        return blockManager.getLoadInfo().toString();
    }
}
