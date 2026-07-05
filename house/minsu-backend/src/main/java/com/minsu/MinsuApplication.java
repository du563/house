package com.minsu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 民宿智能线上预定系统启动类
 */
@SpringBootApplication
@MapperScan("com.minsu.mapper")
public class MinsuApplication {
    public static void main(String[] args) {
        SpringApplication.run(MinsuApplication.class, args);
    }
}
