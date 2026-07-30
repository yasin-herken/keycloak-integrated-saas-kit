package com.archcore.security.config;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWEDecryptionKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.security.Key;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

/**
 * Custom JWT Processor that handles nested JWE(JWS) tokens from Keycloak SPI.
 * <p>
 * Token structure: JWE(JWS(payload)) - an encrypted outer layer containing a signed inner JWT.
 * <p>
 * This processor:
 * 1. Decrypts the outer JWE using the local private key
 * 2. Extracts the inner SignedJWT from the decrypted payload
 * 3. Verifies the inner JWS signature using Keycloak's JWKS
 * 4. Validates and returns the JWT claims
 */
public class NestedJweJWTProcessor extends DefaultJWTProcessor<SecurityContext> {

    private static final Logger logger = LoggerFactory.getLogger(NestedJweJWTProcessor.class);

    private final RSAPrivateKey privateKey;

    public NestedJweJWTProcessor(
            RSAPrivateKey privateKey,
            RSAPublicKey publicKey,
            String keyId,
            String jweAlgorithm,
            String encryptionMethod,
            String jwkSetUri) throws Exception {

        this.privateKey = privateKey;

        JWEAlgorithm algorithm = JWEAlgorithm.parse(jweAlgorithm);
        EncryptionMethod encMethod = EncryptionMethod.parse(encryptionMethod);

        // Configure JWE decryption key selector (local private key)
        JWKSet localJwkSet = new JWKSet(
                new RSAKey.Builder(publicKey)
                        .privateKey(privateKey)
                        .keyID(keyId)
                        .keyUse(KeyUse.ENCRYPTION)
                        .algorithm(new Algorithm(jweAlgorithm))
                        .build()
        );
        setJWEKeySelector(new JWEDecryptionKeySelector<>(
                algorithm,
                encMethod,
                new ImmutableJWKSet<>(localJwkSet)
        ));
        logger.debug("JWE decryption configured: algorithm={}, encMethod={}, keyId={}", jweAlgorithm, encryptionMethod, keyId);

        // Configure JWS verification key selector (Keycloak JWKS)
        JWKSource<SecurityContext> remoteJwkSource = JWKSourceBuilder.create(URI.create(jwkSetUri).toURL()).build();
        setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, remoteJwkSource));
        logger.debug("JWS verification configured with JWKS URI: {}", jwkSetUri);

        // Configure claims verification
        setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>());
    }

    @Override
    public JWTClaimsSet process(JWT jwt, SecurityContext context)
            throws BadJWTException {

        if (jwt instanceof EncryptedJWT encryptedJWT) {
            return processNestedJwe(encryptedJWT, context);
        }

        try {
            return super.process(jwt, context);
        } catch (com.nimbusds.jose.proc.BadJOSEException | com.nimbusds.jose.JOSEException e) {
            throw new BadJWTException("JWT processing failed: " + e.getMessage(), e);
        }
    }

    private JWTClaimsSet processNestedJwe(EncryptedJWT encryptedJWT, SecurityContext context)
            throws BadJWTException {

        try {
            logger.debug("Decrypting outer JWE layer");

            // Step 1: Decrypt the outer JWE
            encryptedJWT.decrypt(new RSADecrypter(privateKey));

            // Step 2: Extract inner signed JWT
            SignedJWT innerJwt = encryptedJWT.getPayload().toSignedJWT();
            if (innerJwt == null) {
                // Plain JWE without inner JWT
                logger.debug("No inner signed JWT found, extracting claims directly");
                JWTClaimsSet claimsSet = encryptedJWT.getJWTClaimsSet();
                getJWTClaimsSetVerifier().verify(claimsSet, context);
                return claimsSet;
            }

            logger.debug("Inner signed JWT detected, verifying signature via JWKS");

            // Step 3: Verify inner JWS signature
            JWSHeader header = innerJwt.getHeader();
            List<? extends Key> matchingKeys = getJWSKeySelector().selectJWSKeys(header, context);

            if (matchingKeys == null || matchingKeys.isEmpty()) {
                throw new BadJWTException("No matching JWS key found for kid: " + header.getKeyID());
            }

            boolean signatureValid = false;
            for (Key key : matchingKeys) {
                if (key instanceof RSAPublicKey rsaPublicKey) {
                    RSASSAVerifier verifier = new RSASSAVerifier(rsaPublicKey);
                    if (innerJwt.verify(verifier)) {
                        signatureValid = true;
                        break;
                    }
                }
            }

            if (!signatureValid) {
                throw new BadJWTException("Inner JWT signature verification failed for kid: " + header.getKeyID());
            }

            // Step 4: Extract and verify claims
            JWTClaimsSet claimsSet = innerJwt.getJWTClaimsSet();
            getJWTClaimsSetVerifier().verify(claimsSet, context);

            logger.debug("Nested JWE(JWS) token processed successfully. Subject: {}", claimsSet.getSubject());
            return claimsSet;

        } catch (com.nimbusds.jose.JOSEException e) {
            throw new BadJWTException("JWE/JWS processing failed: " + e.getMessage(), e);
        } catch (java.text.ParseException e) {
            throw new BadJWTException("Failed to parse JWT claims: " + e.getMessage(), e);
        }
    }
}
