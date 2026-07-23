package com.archcore.keycloak.spi.jwe;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

public class JwksClient {

    private static final Logger logger = LoggerFactory.getLogger(JwksClient.class);

    private final String jwksUrl;
    private final long cacheTtlMs;
    private final HttpClient httpClient;
    private final AtomicReference<CachedJwks> cache;

    public JwksClient(String jwksUrl, long cacheTtlMs) {
        this.jwksUrl = jwksUrl;
        this.cacheTtlMs = cacheTtlMs;
        this.httpClient = HttpClient.newHttpClient();
        this.cache = new AtomicReference<>();
    }

    public JwksClient(String jwksUrl, long cacheTtlMs, HttpClient httpClient) {
        this.jwksUrl = jwksUrl;
        this.cacheTtlMs = cacheTtlMs;
        this.httpClient = httpClient;
        this.cache = new AtomicReference<>();
    }

    public RSAPublicKey getPublicKey() {
        CachedJwks cached = cache.get();
        if (cached != null && !cached.isExpired()) {
            logger.debug("Returning cached JWKS public key");
            return cached.publicKey;
        }

        logger.info("Fetching JWKS from: {}", jwksUrl);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(jwksUrl))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("JWKS endpoint returned status: " + response.statusCode());
            }

            JWKSet jwkSet = JWKSet.parse(response.body());
            RSAKey rsaKey = (RSAKey) jwkSet.getKeys().getFirst();
            RSAPublicKey publicKey = rsaKey.toRSAPublicKey();

            cache.set(new CachedJwks(publicKey, Instant.now(), cacheTtlMs));
            logger.info("JWKS public key fetched and cached successfully");
            return publicKey;
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to fetch JWKS from: {}", jwksUrl, e);
            if (cached != null) {
                logger.warn("Returning stale cached JWKS due to fetch failure");
                return cached.publicKey;
            }
            throw new RuntimeException("JWKS fetch failed and no cache available", e);
        } catch (Exception e) {
            logger.error("Failed to parse JWKS response", e);
            throw new RuntimeException("JWKS parse failed", e);
        }
    }

    private record CachedJwks(RSAPublicKey publicKey, Instant fetchedAt, long ttlMs) {

        boolean isExpired() {
                return Instant.now().isAfter(fetchedAt.plusMillis(ttlMs));
            }
        }
}
