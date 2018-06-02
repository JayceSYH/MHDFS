package com.syh.mdfs.nameserver.Controllers;

import Config.ProtoConfig;
import com.netflix.discovery.EurekaClient;
import com.syh.mdfs.nameserver.Models.MDFSDirMetaData;
import com.syh.mdfs.nameserver.Models.MDFSObjectMetaData;
import com.syh.mdfs.nameserver.Services.FileManager;
import com.syh.mdfs.nameserver.Services.NodeManager;
import com.syh.mdfs.nameserver.View.RespPageMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class NameServerControllers {

    @Autowired
    NodeManager nodeManager;

    @Autowired
    FileManager fileManager;

    @Autowired
    private HttpServletRequest request;

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getFileContent() {

        try {
            String path = request.getServletPath();

            MDFSObjectMetaData objectMetaData = fileManager.getObjMetaData(path);

            if (objectMetaData == null)
                return new ResponseEntity<byte[]>(HttpStatus.BAD_REQUEST);

            if (objectMetaData.isDir()) {
                List<MDFSObjectMetaData> metaDataList = objectMetaData.getDirMetaData().listDir();
                return new ResponseEntity<byte[]>(RespPageMaker.makeDirBrowsePage(path, metaDataList).getBytes(), HttpStatus.ACCEPTED);
            }
            else {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment",  java.net.URLEncoder.encode(objectMetaData.getName(), "UTF-8"));
                return new ResponseEntity<byte[]>(fileManager.downloadFile(request.getServletPath()), headers, HttpStatus.ACCEPTED);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<byte[]>(e.getMessage().getBytes(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/**", method = RequestMethod.POST)
    public ResponseEntity<String> createFileOrDirectory(@RequestParam(value = "content", required = false)MultipartFile multipartFile,
                                       @RequestParam(value = "name", required = false)String name, @RequestParam("objType")String objType) {
        String path = request.getServletPath();

        MDFSObjectMetaData objectMetaData = fileManager.getObjMetaData(path);
        try {
            if (objType.equals(ProtoConfig.objDir)) {
                if (name != null && !name.trim().equals(""))
                    fileManager.createDir(request.getServletPath(), name);
                List<MDFSObjectMetaData> metaDataList = objectMetaData.getDirMetaData().listDir();
                return new ResponseEntity<String>(RespPageMaker.makeDirBrowsePage(path, metaDataList), HttpStatus.CREATED);
            }
            else {
                if (multipartFile != null && multipartFile.getSize() > 0) {
                    byte[] content = multipartFile.getBytes();
                    fileManager.uploadFile(request.getServletPath(), multipartFile.getOriginalFilename(), content);
                }
                List<MDFSObjectMetaData> metaDataList = objectMetaData.getDirMetaData().listDir();
                return new ResponseEntity<String>(RespPageMaker.makeDirBrowsePage(path, metaDataList), HttpStatus.CREATED);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            List<MDFSObjectMetaData> metaDataList = objectMetaData.getDirMetaData().listDir();
            return new ResponseEntity<String>(RespPageMaker.makeDirBrowsePage(path, metaDataList), HttpStatus.CREATED);
        }
    }

    @RequestMapping(value = "/**", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteFileOrDirectory() {
        String path = request.getServletPath();

        MDFSObjectMetaData objectMetaData = fileManager.getObjMetaData(path);
        if (objectMetaData == null || path.trim().equals("/"))
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);

        fileManager.removeObj(objectMetaData.getPath());
        return new ResponseEntity<String>(objectMetaData.getParentDir(), HttpStatus.ACCEPTED);
    }
}
