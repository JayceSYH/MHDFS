package com.syh.mdfs.testclient;

import com.netflix.discovery.EurekaClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
public class Controller {
    @Resource
    private EurekaClient client; //进行Eureka的发现服务


    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String discover() {//直接返回发现服务信息

        return "<html>\n" +
                "\t<head>\n" +
                "\t<script type=\"text/javascript\" src=\"http://code.jquery.com/jquery-latest.js\"></script>\n" +
                "<script type=\"text/javascript\">\n" +
                "\n" +
                "function test() {\n" +
                "\t$.ajax({\n" +
                "\t    url: 'http://localhost:8917/testpost',\n" +
                "\t    type: 'DELETE',\n" +
                "\t    success: function(result) {\n" +
                "\t        window.location.href='http://localhost:8917/test1';\n" +
                "\t    }\n" +
                "\t});\n" +
                "}\n" +
                "</script>\n" +
                "</head>\n" +
                "\n" +
                "\t<body>\n" +
                "\t<button onclick=\"test()\">HH</button>\n" +
                "\t</body>\n" +
                "\t\n" +
                "</html>";

    }

    @RequestMapping(value = "/test1", method = RequestMethod.GET)
    public String discover1() {//直接返回发现服务信息

        return "<html>\n" +
                "\t<head>\n" +
                "\t<script type=\"text/javascript\" src=\"http://code.jquery.com/jquery-latest.js\"></script>\n" +
                "<script type=\"text/javascript\">\n" +
                "\n" +
                "function test() {\n" +
                "\t$.ajax({\n" +
                "\t    url: 'http://localhost:8917/testpost',\n" +
                "\t    type: 'DELETE',\n" +
                "\t    success: function(result) {\n" +
                "\t        // Do something with the result\n" +
                "\t    }\n" +
                "\t});\n" +
                "}\n" +
                "</script>\n" +
                "</head>\n" +
                "\n" +
                "\t<body>\n" +
                "\t<button onclick=\"test()\">HH1</button>\n" +
                "\t</body>\n" +
                "\t\n" +
                "</html>";

    }

    @RequestMapping(value = "/testpost", method= RequestMethod.DELETE)
    public String test() {
        try {
            System.out.println("ok");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "ok";
    }
}
