package com.archcore.security.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwePropertiesTest {

    @Test
    void defaults_shouldUseClasspathKeys() {
        JweProperties properties = new JweProperties();

        assertNotNull(properties.getPrivateKeyLocation());
        assertNotNull(properties.getPublicKeyLocation());
        assertEquals("classpath:keys/test-private.pem", properties.getPrivateKeyLocation());
        assertEquals("classpath:keys/test-public.pem", properties.getPublicKeyLocation());
    }

    @Test
    void setters_shouldOverrideDefaults() {
        JweProperties properties = new JweProperties();

        properties.setPrivateKeyLocation("/etc/secrets/jwe-private.pem");
        properties.setPublicKeyLocation("/etc/secrets/jwe-public.pem");

        assertEquals("/etc/secrets/jwe-private.pem", properties.getPrivateKeyLocation());
        assertEquals("/etc/secrets/jwe-public.pem", properties.getPublicKeyLocation());
    }
}
