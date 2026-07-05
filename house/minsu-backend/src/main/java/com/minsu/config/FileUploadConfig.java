package com.minsu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.io.IOException;

@Configuration
public class FileUploadConfig {
    // 必须命名为 multipartResolver，强制覆盖 Spring 默认解析器
    @Bean("multipartResolver")
    public MultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        // 1. 单个文件最大 10MB
        resolver.setMaxUploadSizePerFile(10 * 1024 * 1024);
        // 2. 单次请求总文件最大 50MB
        resolver.setMaxUploadSize(50 * 1024 * 1024);
        // 3. 自定义临时目录
        try {
            resolver.setUploadTempDir(new org.springframework.core.io.FileSystemResource("D:/temp/upload"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //CommonsMultipartResolver 会自动清理临时文件，无需手动设置
        return resolver;
    }
}