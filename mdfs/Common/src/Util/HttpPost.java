package Util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class HttpPost {
    public static String formUploadRetString(String urlStr, Map<String, String> textMap,
                                             Map<String, byte[]> fileMap) {
        byte[] ret = formUpload(urlStr, textMap, fileMap);
        return new String(ret);
    }

    public static byte[] formUploadRetBytes(String urlStr, Map<String, String> textMap,
                                            Map<String, byte[]> fileMap) {
        return formUpload(urlStr, textMap, fileMap);
    }

    @SuppressWarnings("rawtypes")
    private static byte[] formUpload(String urlStr, Map<String, String> textMap,
                                    Map<String, byte[]> fileMap) {
        String res = "";
        HttpURLConnection conn = null;
        // boundary就是request头和上传文件内容的分隔符
        String BOUNDARY = "---------------------------123821742118716";
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + BOUNDARY);
            OutputStream out = new DataOutputStream(conn.getOutputStream());
            // text
            if (textMap != null) {
                StringBuilder strBuf = new StringBuilder();
                for (Object o : textMap.entrySet()) {
                    Map.Entry entry = (Map.Entry) o;
                    String inputName = (String) entry.getKey();
                    String inputValue = (String) entry.getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    strBuf.append("\r\n").append("--").append(BOUNDARY)
                            .append("\r\n");
                    strBuf.append("Content-Disposition: form-data; name=\"").append(inputName).append("\"\r\n\r\n");
                    strBuf.append(inputValue);
                }
                out.write(strBuf.toString().getBytes());
            }
            // file
            if (fileMap != null) {
                for (Object o : fileMap.entrySet()) {
                    Map.Entry entry = (Map.Entry) o;
                    String inputName = (String) entry.getKey();
                    byte[] inputValue = (byte[]) entry.getValue();

                    String contentType = "application/octet-stream";
                    String strBuf = "\r\n" + "--" + BOUNDARY +
                            "\r\n" +
                            "Content-Disposition: form-data; name=\""
                            + inputName + "\"; filename=\"" + inputName
                            + "\"\r\n" +
                            "Content-Type:" + contentType + "\r\n\r\n";
                    out.write(strBuf.getBytes());
                    out.write(inputValue);
                }
            }
            byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
            out.write(endData);
            out.flush();
            out.close();
            // 读取返回数据
            InputStream inputStream = conn.getInputStream();
            int contentLength = conn.getContentLength();
            byte[] content = new byte[contentLength];
            byte[] buffer = new byte[1024 * 1024];
            int readLen = 0;
            int destPos = 0;
            while ((readLen = inputStream.read(buffer)) > 0) {
                System.arraycopy(buffer, 0, content, destPos, readLen);
                destPos += readLen;
            }

            return content;

        } catch (Exception e) {
            System.out.println("发送POST请求出错。" + urlStr);
            e.printStackTrace();
            return new byte[0];
        } finally {
            if (conn != null) {
                conn.disconnect();
                conn = null;
            }
        }
    }
}