package com.archcore.app.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.util.Map;
import java.util.List;

@TestConfiguration
public class TestSecurityConfig {

    public static final String TEST_TOKEN = "test-valid-token";

    @Bean
    @Primary
    public JwtDecoder testJwtDecoder() {
        return token -> {
            if ("invalid-token".equals(token)) {
                throw new JwtException("Invalid token");
            }
            return Jwt.withTokenValue(token)
                    .header("alg", "RS256")
                    .subject("test-user-id")
                    .claim("iss", "http://localhost:8080/realms/test")
                    .claim("realm_access", Map.of("roles", List.of("user")))
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
        };
    }
}
