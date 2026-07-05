package com.minsu.controller;

import com.minsu.common.Result;
import com.minsu.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/file")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * 单文件上传
     * @param file 上传的文件
     * @return 文件访问 URL
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        
        try {
            String url = fileUploadService.uploadFile(file);
            return Result.success("文件上传成功", url);
        } catch (Exception e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 用户头像上传（固定上传到 upload 桶的 user/ 目录下）
     * 一个用户只保存一个头像
     */
    @PostMapping("/upload/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        try {
            String original = file.getOriginalFilename();
            String safeName = (original == null ? "avatar" : original).replaceAll("[\\\\/]", "_");
            String objectName = "user/" + System.currentTimeMillis() + "_" + safeName;
            String url = fileUploadService.uploadFile(file, objectName);
            return Result.success("头像上传成功", url);
        } catch (Exception e) {
            return Result.error("头像上传失败：" + e.getMessage());
        }
    }

    /**
     * 多文件上传
     * @param files 上传的文件数组
     * @return 文件访问 URL 列表
     */
    @PostMapping("/uploads")
    public Result<java.util.List<String>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Result.error("上传文件不能为空");
        }
        
        try {
            java.util.List<String> urls = new java.util.ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url = fileUploadService.uploadFile(file);
                    urls.add(url);
                }
            }
            return Result.success("文件批量上传成功，共上传 " + urls.size() + " 个文件", urls);
        } catch (Exception e) {
            return Result.error("文件批量上传失败：" + e.getMessage());
        }
    }
}
