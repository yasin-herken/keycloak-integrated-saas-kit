package com.archcore.keycloak.spi.jwe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JweProviderConfig {

    static final String PROP_JWKS_URL = "archcore.jwe.default-jwks-url";
    static final String PROP_CACHE_TTL = "archcore.jwe.cache-ttl-ms";
    static final String PROP_ALGORITHM = "archcore.jwe.algorithm";
    static final String PROP_ENCRYPTION_METHOD = "archcore.jwe.encryption-method";
    static final String PROP_KEY_ID = "archcore.jwe.key-id";

    static final String DEFAULT_JWKS_URL = "http://host.docker.internal:8081/.well-known/jwks.json";
    static final long DEFAULT_CACHE_TTL_MS = 5 * 60 * 1000L;
    static final String DEFAULT_ALGORITHM = "RSA-OAEP-256";
    static final String DEFAULT_ENCRYPTION_METHOD = "A256GCM";
    static final String DEFAULT_KEY_ID = "archcore-enc-key";

    private String defaultJwksUrl;
    private long defaultCacheTtlMs;
    private String jweAlgorithm;
    private String encryptionMethod;
    private String keyId;

    static JweProviderConfig fromScope(org.keycloak.Config.Scope config) {
        if (config == null) {
            return new JweProviderConfig(
                    DEFAULT_JWKS_URL,
                    DEFAULT_CACHE_TTL_MS,
                    DEFAULT_ALGORITHM,
                    DEFAULT_ENCRYPTION_METHOD,
                    DEFAULT_KEY_ID
            );
        }
        return new JweProviderConfig(
                config.get(PROP_JWKS_URL, DEFAULT_JWKS_URL),
                Long.parseLong(config.get(PROP_CACHE_TTL, String.valueOf(DEFAULT_CACHE_TTL_MS))),
                config.get(PROP_ALGORITHM, DEFAULT_ALGORITHM),
                config.get(PROP_ENCRYPTION_METHOD, DEFAULT_ENCRYPTION_METHOD),
                config.get(PROP_KEY_ID, DEFAULT_KEY_ID)
        );
    }
}
