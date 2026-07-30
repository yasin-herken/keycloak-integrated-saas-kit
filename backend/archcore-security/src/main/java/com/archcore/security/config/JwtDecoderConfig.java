package com.archcore.security.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(JweProperties.class)
public class JwtDecoderConfig {

    private static final Logger logger = LoggerFactory.getLogger(JwtDecoderConfig.class);

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;
    private final String issuerUri;
    private final JweProperties jweProperties;

    public JwtDecoderConfig(
            ResourceLoader resourceLoader,
            JweProperties jweProperties,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) throws Exception {

        this.issuerUri = issuerUri;
        this.jweProperties = jweProperties;
        this.privateKey = loadPrivateKey(resourceLoader, jweProperties.getPrivateKeyLocation());
        this.publicKey = loadPublicKey(resourceLoader, jweProperties.getPublicKeyLocation());

        logger.info("JWE keys loaded successfully. Private key location: {}, Public key location: {}",
                jweProperties.getPrivateKeyLocation(), jweProperties.getPublicKeyLocation());
    }

    @Bean
    public RSAPublicKey archcorePublicKey() {
        return this.publicKey;
    }

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        String jwkSetUri = issuerUri + "/protocol/openid-connect/certs";
        NestedJweJWTProcessor processor = new NestedJweJWTProcessor(
                privateKey,
                publicKey,
                jweProperties.getKeyId(),
                jweProperties.getJweAlgorithm(),
                jweProperties.getEncryptionMethod(),
                jwkSetUri);
        logger.info("NestedJweJWTProcessor created. Issuer: {}, JWKS: {}, algorithm: {}/{}, keyId: {}",
                issuerUri, jwkSetUri,
                jweProperties.getJweAlgorithm(), jweProperties.getEncryptionMethod(),
                jweProperties.getKeyId());
        return new NimbusJwtDecoder(processor);
    }

    private RSAPrivateKey loadPrivateKey(ResourceLoader resourceLoader, String location) throws Exception {
        logger.info("Loading private key from: {}", location);
        String pemContent = readPemContent(resourceLoader, location);

        String cleanedPem = pemContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(cleanedPem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        RSAPrivateKey key = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        logger.info("Private key loaded successfully from: {}", location);

        return key;
    }

    private RSAPublicKey loadPublicKey(ResourceLoader resourceLoader, String location) throws Exception {
        logger.info("Loading public key from: {}", location);
        String pemContent = readPemContent(resourceLoader, location);

        String cleanedPem = pemContent
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(cleanedPem);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        RSAPublicKey key = (RSAPublicKey) keyFactory.generatePublic(keySpec);
        logger.info("Public key loaded successfully from: {}", location);

        return key;
    }

    private String readPemContent(ResourceLoader resourceLoader, String location) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resourceLoader.getResource(location).getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
