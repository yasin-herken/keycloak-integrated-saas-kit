package com.archcore.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityConfigTest {

    @Test
    void securityFilterChain_shouldBeConfigured() throws Exception {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getResource("classpath:keys/test-private.pem"))
                .thenReturn(new ClassPathResource("keys/test-private.pem"));
        when(resourceLoader.getResource("classpath:keys/test-public.pem"))
                .thenReturn(new ClassPathResource("keys/test-public.pem"));

        JweProperties jweProperties = new JweProperties();
        JwtDecoderConfig jwtDecoderConfig = new JwtDecoderConfig(resourceLoader, jweProperties, "http://localhost:8080/realms/archcore");

        SecurityConfig securityConfig = new SecurityConfig();
        assertNotNull(securityConfig);
        assertNotNull(jwtDecoderConfig.archcorePublicKey());
    }

    @Test
    void securityConfig_shouldBeConfiguration() {
        assertTrue(SecurityConfig.class.isAnnotationPresent(
                org.springframework.context.annotation.Configuration.class));
    }
}
