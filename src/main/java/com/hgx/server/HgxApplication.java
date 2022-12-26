package com.hgx.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hgx.server.dao")
public class HgxApplication {
    public static void main(String[] args) {
        SpringApplication.run(HgxApplication.class, args);
    }
}
