package com.archcore.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "archcore.security")
@Getter
@Setter
public class SecurityProperties {

    private List<String> allowedOrigins = List.of("https://localhost:3000");
}
