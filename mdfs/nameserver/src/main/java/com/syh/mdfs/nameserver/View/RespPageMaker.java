package com.syh.mdfs.nameserver.View;

import com.syh.mdfs.nameserver.Models.MDFSObjectMetaData;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RespPageMaker {

    private static Map<String, String> templateMap = new HashMap<>();
    private static ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public static String makeDirBrowsePage(String dirPath, List<MDFSObjectMetaData> metaDataList) {
        String template = loadTemplate("/templates/DirBrowsePageTemplate.html");
        template = template.replace("${{title}}", dirPath);
        String parentDir = "/";
        if (!dirPath.equals("/"))
            parentDir = dirPath.substring(0, dirPath.substring(0, dirPath.length() - 1).lastIndexOf("/") + 1);
        StringBuilder body = new StringBuilder();
        body.append("当前位置: ").append(dirPath).append("<br>");
        body.append("上级目录: <a href='").append(parentDir).append("'> ").append(parentDir).append("</a>");
        body.append("<br><br>").append("________________________________________________<br>");
        for (MDFSObjectMetaData objectMetaData : metaDataList) {
            String fileSizePart = "文件大小: " +
                    String.format("% 10d", (int)(objectMetaData.getSize() + 1023) / 1024).replace(" ", "&nbsp") +
                    "KB&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp";
            body.append("<button onclick=\"deleteFileOrDirectory(").append("'").append(objectMetaData.getPath()).
                    append("'").append(")\">删除</button>&nbsp&nbsp");
            if (objectMetaData.isDir())
                body.append("<目录>&nbsp&nbsp&nbsp&nbsp").append(fileSizePart).append("<a href='").append(objectMetaData.getPath()).append("'>").
                        append(objectMetaData.getName()).append("</a><br>");
            else
                body.append("<文件>&nbsp&nbsp&nbsp&nbsp").append(fileSizePart).append("<a href='").append(objectMetaData.getPath()).append("'>").
                        append(objectMetaData.getName()).append("</a><br>");
        }
        template = template.replace("${{body}}", body.toString());
        template = template.replace("${{create_url}}", dirPath);
        return template;
    }

    private static String loadTemplate(String path) {
        readWriteLock.readLock().lock();
        try {
            if (templateMap.containsKey(path))
                return templateMap.get(path);
        }
        finally {
            readWriteLock.readLock().unlock();
        }

        Resource resource = new ClassPathResource(path);
        try {
            File file = resource.getFile();
            FileInputStream inputStream = new FileInputStream(file);
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            String strResult = result.toString("UTF-8");
            readWriteLock.writeLock().lock();
            templateMap.put(path, strResult);
            readWriteLock.writeLock().unlock();
            return strResult;
        }
        catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
