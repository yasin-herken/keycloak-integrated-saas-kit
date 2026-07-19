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
public class JwtDecoderConfig {

    private static final Logger logger = LoggerFactory.getLogger(JwtDecoderConfig.class);

    private static final String JWE_ALGORITHM = "RSA-OAEP-256";
    private static final String ENCRYPTION_METHOD = "A256GCM";
    private static final String KEY_ID = "archcore-enc-key";

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;
    private final String issuerUri;

    public JwtDecoderConfig(
            ResourceLoader resourceLoader,
            @Value("${archcore.security.jwe.private-key-location:classpath:keys/test-private.pem}") String privateKeyLocation,
            @Value("${archcore.security.jwe.public-key-location:classpath:keys/test-public.pem}") String publicKeyLocation,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) throws Exception {

        this.issuerUri = issuerUri;
        this.privateKey = loadPrivateKey(resourceLoader, privateKeyLocation);
        this.publicKey = loadPublicKey(resourceLoader, publicKeyLocation);

        logger.info("JWE keys loaded successfully. Private key location: {}, Public key location: {}",
                privateKeyLocation, publicKeyLocation);
    }

    @Bean
    public RSAPublicKey archcorePublicKey() {
        return this.publicKey;
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
        return new JWKSet(
            new RSAKey.Builder(this.publicKey)
                .privateKey(this.privateKey)
                .keyID(KEY_ID)
                .keyUse(KeyUse.ENCRYPTION)
                .algorithm(new com.nimbusds.jose.Algorithm(JWE_ALGORITHM))
                .build()
        );
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
