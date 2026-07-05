package com.minsu.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {
    
    /**
     * 上传文件到 MinIO
     * @param file 上传的文件
     * @return 文件访问 URL
     */
    String uploadFile(MultipartFile file);
    
    /**
     * 上传文件到 MinIO 指定路径
     * @param file 上传的文件
     * @param objectName 对象名称（包含路径）
     * @return 文件访问 URL
     */
    String uploadFile(MultipartFile file, String objectName);
}
