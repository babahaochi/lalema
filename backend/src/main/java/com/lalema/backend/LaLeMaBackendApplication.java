package com.lalema.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.lalema.backend.mapper")
@EnableScheduling
public class LaLeMaBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(LaLeMaBackendApplication.class, args);
    }
}
