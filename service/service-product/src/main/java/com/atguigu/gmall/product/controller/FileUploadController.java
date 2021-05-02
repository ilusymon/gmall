package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.product.test.TestFdfs;
import com.atguigu.gmall.result.Result;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin
@RequestMapping("admin/product/")
public class FileUploadController {

    @RequestMapping("fileUpload")
    public Result fileUpload(@RequestParam("file") MultipartFile file){

        String url = "http://192.168.200.128:8080";
        String path = FileUploadController.class.getClassLoader().getResource("tracker.conf").getPath();
        System.out.println(path);

        try {
            ClientGlobal.init(path);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        // 获得tracker连接
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer connection = null;
        try {
            connection = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 通过tracker获得storage
        StorageClient storageClient = new StorageClient(connection,null);

        // 上传文件
        String[] jpgs = new String[0];
        try {
            String originalFilename = file.getOriginalFilename();
//            int i = originalFilename.lastIndexOf(".");
//            String substring = originalFilename.substring(i + 1);
            jpgs = storageClient.upload_file(file.getBytes(), StringUtils.getFilenameExtension(originalFilename), null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        // 返回url
        for (String jpg : jpgs) {
            url = url + "/" + jpg;
        }
        System.out.println(url);
        return Result.ok(url);
    }

}
