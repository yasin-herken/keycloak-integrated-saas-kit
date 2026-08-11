package com.archcore.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.archcore")
@EntityScan(basePackages = "com.archcore.core.domain")
@EnableJpaRepositories(basePackages = "com.archcore.core.repository")
public class ArchCoreApplication {

    static void main(String[] args) {
        SpringApplication.run(ArchCoreApplication.class, args);
    }
}
