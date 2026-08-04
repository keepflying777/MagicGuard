package com.magicguard;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.magicguard.repository")
public class MagicGuardApplication {
    public static void main(String[] args) {
        SpringApplication.run(MagicGuardApplication.class, args);
    }
}
