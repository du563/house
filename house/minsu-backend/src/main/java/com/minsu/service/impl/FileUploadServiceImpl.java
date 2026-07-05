package com.minsu.service.impl;

import com.minsu.service.FileUploadService;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * 文件上传服务实现类
 */
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.bucket-name}")
    private String bucketName;

    private final io.minio.MinioClient minioClient;

    public FileUploadServiceImpl(io.minio.MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public String uploadFile(MultipartFile file) {
        // 使用原始文件名作为对象名
        String objectName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        return uploadFile(file, objectName);
    }

    @Override
    public String uploadFile(MultipartFile file, String objectName) {
        try {
            // 检查 bucket 是否存在，不存在则创建
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            // 设置 bucket 为公共读写
            setBucketPublic();

            // 上传文件
            InputStream inputStream = file.getInputStream();
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, inputStream.available(), -1)
                    .contentType(file.getContentType())
                    .build();

            minioClient.putObject(args);
            // 生成文件访问 URL
            return getFileUrl(objectName);

        } catch (Exception e) {
            throw new RuntimeException("文件上传失败：" + e.getMessage(), e);
        }
    }

    /**
     * 设置 Bucket 为公共读写
     */
    private void setBucketPublic() {
        try {
            String policyJson = "{\n" +
                    "    \"Version\": \"2012-10-17\",\n" +
                    "    \"Statement\": [\n" +
                    "        {\n" +
                    "            \"Effect\": \"Allow\",\n" +
                    "            \"Principal\": {\"AWS\": [\"*\"]},\n" +
                    "            \"Action\": [\"s3:GetObject\"],\n" +
                    "            \"Resource\": [\"arn:aws:s3:::" + bucketName + "/*\"]\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}";
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(policyJson)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("设置 Bucket 公共访问失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取文件访问 URL
     */
    private String getFileUrl(String objectName) {
        try {
            // 生成带签名的 URL
            // 如果 bucket 已经是公开的，可以直接使用公开 URL
            String presignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            
            // 对于公开 bucket，返回不带签名的 URL
            return endpoint + "/" + bucketName + "/" + objectName;
            
        } catch (Exception e) {
            throw new RuntimeException("获取文件 URL 失败：" + e.getMessage(), e);
        }
    }
}
