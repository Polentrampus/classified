package com.classified;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "com.classified")
@EntityScan(basePackages = "com.classified.entity")
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
