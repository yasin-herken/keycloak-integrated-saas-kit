package com.archcore.security.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.security.interfaces.RSAPublicKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtDecoderConfigTest {

    private ResourceLoader resourceLoader;
    private JweProperties jweProperties;

    @BeforeEach
    void setUp() {
        resourceLoader = mock(ResourceLoader.class);
        jweProperties = new JweProperties();
        when(resourceLoader.getResource(any(String.class)))
                .thenAnswer(invocation -> {
                    String location = invocation.getArgument(0);
                    return new ClassPathResource(location.replace("classpath:", ""));
                });
    }

    @Test
    void constructor_shouldLoadKeysFromClasspath() throws Exception {
        JweProperties props = new JweProperties();
        props.setPrivateKeyLocation("classpath:keys/test-private.pem");
        props.setPublicKeyLocation("classpath:keys/test-public.pem");

        JwtDecoderConfig config = new JwtDecoderConfig(resourceLoader, props, "http://localhost:8080/realms/archcore");

        assertNotNull(config.archcorePublicKey());
        assertNotNull(config.jwtDecoder());
    }

    @Test
    void archcorePublicKey_shouldReturnValidRsaKey() throws Exception {
        JwtDecoderConfig config = new JwtDecoderConfig(resourceLoader, jweProperties, "http://localhost:8080/realms/archcore");

        RSAPublicKey publicKey = config.archcorePublicKey();
        assertNotNull(publicKey);
        assertEquals("RSA", publicKey.getAlgorithm());
    }

    @Test
    void jwtDecoder_shouldCreateDecoder() throws Exception {
        JwtDecoderConfig config = new JwtDecoderConfig(resourceLoader, jweProperties, "http://localhost:8080/realms/archcore");

        JwtDecoder decoder = config.jwtDecoder();
        assertNotNull(decoder);
    }

    @Test
    void constructor_shouldHandleCustomKeyLocations() throws Exception {
        jweProperties.setPrivateKeyLocation("classpath:keys/test-private.pem");
        jweProperties.setPublicKeyLocation("classpath:keys/test-public.pem");

        JwtDecoderConfig config = new JwtDecoderConfig(resourceLoader, jweProperties, "http://localhost:8080/realms/archcore");

        assertNotNull(config.archcorePublicKey());
    }

    @Test
    void constructor_shouldFailWithInvalidKeyContent() {
        ResourceLoader badLoader = mock(ResourceLoader.class);
        when(badLoader.getResource(any(String.class)))
                .thenAnswer(invocation -> {
                    org.springframework.core.io.Resource resource = mock(org.springframework.core.io.Resource.class);
                    when(resource.getInputStream())
                            .thenReturn(new java.io.ByteArrayInputStream("invalid-key-content".getBytes()));
                    return resource;
                });

        JweProperties props = new JweProperties();
        props.setPrivateKeyLocation("classpath:bad-key.pem");
        props.setPublicKeyLocation("classpath:bad-key.pem");

        assertThrows(Exception.class, () ->
                new JwtDecoderConfig(badLoader, props, "http://localhost:8080/realms/archcore"));
    }
}
