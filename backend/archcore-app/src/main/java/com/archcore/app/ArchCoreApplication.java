package com.archcore.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.archcore")
public class ArchCoreApplication {

    static void main(String[] args) {
        SpringApplication.run(ArchCoreApplication.class, args);
    }
}
