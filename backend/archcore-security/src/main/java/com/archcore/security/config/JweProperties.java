package com.archcore.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "archcore.security.jwe")
public class JweProperties {

    private String privateKeyLocation = "classpath:keys/test-private.pem";
    private String publicKeyLocation = "classpath:keys/test-public.pem";
}
