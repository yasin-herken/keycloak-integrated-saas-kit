package com.archcore.security.config;

import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWEDecryptionKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * Configuration for JWE (JSON Web Encryption) JWT Decoder.
 * 
 * Unified approach: Loads private key from a file path.
 * Works with both Docker (volume mount) and Kubernetes (Secret volume mount).
 * 
 * Usage:
 * - Docker: Mount key file to a path, set JWE_PRIVATE_KEY_LOCATION
 * - Kubernetes: Create Secret, mount to pod, set JWE_PRIVATE_KEY_LOCATION
 */
@Configuration
public class JwtDecoderConfig {

    private static final Logger logger = LoggerFactory.getLogger(JwtDecoderConfig.class);

    private static final String JWE_ALGORITHM = "RSA-OAEP-256";
    private static final String ENCRYPTION_METHOD = "A256GCM";

    private final RSAPrivateCrtKey privateKey;
    private final String issuerUri;

    public JwtDecoderConfig(
            @Value("${archcore.security.jwe.private-key-location:/etc/secrets/jwe-private.pem}") String privateKeyLocation,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) throws Exception {
        
        this.issuerUri = issuerUri;
        this.privateKey = loadPrivateKey(privateKeyLocation);
        
        logger.info("JWE JwtDecoder configured with key location: {}", privateKeyLocation);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(issuerUri + "/protocol/openid-connect/certs")
                .jwtProcessorCustomizer(this::configureJweDecryption)
                .build();
        
        logger.info("JWE JwtDecoder created with issuer URI: {}", issuerUri);
        return decoder;
    }

    private void configureJweDecryption(ConfigurableJWTProcessor<SecurityContext> jwtProcessor) {
        JWKSource<SecurityContext> jweJwkSource = new ImmutableJWKSet<>(createJwkSet());
        JWEDecryptionKeySelector<SecurityContext> jweKeySelector = new JWEDecryptionKeySelector<>(
                JWEAlgorithm.parse(JWE_ALGORITHM),
                EncryptionMethod.parse(ENCRYPTION_METHOD),
                jweJwkSource);

        jwtProcessor.setJWEKeySelector(jweKeySelector);
        logger.debug("JWE decryption configured with algorithm: {}, encryption method: {}", 
                JWE_ALGORITHM, ENCRYPTION_METHOD);
    }

    private JWKSet createJwkSet() {
        RSAPublicKey publicKey = (RSAPublicKey) this.privateKey;
        return new JWKSet(
            new RSAKey.Builder(publicKey)
                .privateKey(this.privateKey)
                .keyUse(KeyUse.ENCRYPTION)
                .build()
        );
    }

    private RSAPrivateCrtKey loadPrivateKey(String location) throws Exception {
        logger.info("Loading private key from: {}", location);
        
        // Read the PEM file
        Path keyPath = Paths.get(location);
        if (!Files.exists(keyPath)) {
            throw new IllegalArgumentException("Private key file not found: " + location);
        }
        
        String pemContent = Files.readString(keyPath);
        
        // Remove PEM headers and footers
        String cleanedPem = pemContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(cleanedPem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        
        RSAPrivateCrtKey key = (RSAPrivateCrtKey) keyFactory.generatePrivate(keySpec);
        logger.info("Private key loaded successfully from: {}", location);
        
        return key;
    }
}