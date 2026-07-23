package com.archcore.keycloak.spi.jwe;

import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.jwk.RSAKey;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenResponseMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JweAccessTokenResponseMapper extends AbstractOIDCProtocolMapper
        implements OIDCAccessTokenResponseMapper {

    private static final Logger logger = LoggerFactory.getLogger(JweAccessTokenResponseMapper.class);

    public static final String PROVIDER_ID = "archcore-jwe-access-token-response-mapper";
    public static final String TARGET_CLIENT_CONFIG = "target-client";
    public static final String JWKS_URL_CONFIG = "jwks-url";
    public static final String CACHE_TTL_MS_CONFIG = "cache-ttl-ms";

    private static final String DEFAULT_JWKS_URL = "http://host.docker.internal:8081/.well-known/jwks.json";
    private static final String DEFAULT_TARGET_CLIENT = "archcore-client";
    private static final long DEFAULT_CACHE_TTL_MS = 5 * 60 * 1000L;

    private static final String JWE_ALGORITHM = "RSA-OAEP-256";
    private static final String ENCRYPTION_METHOD = "A256GCM";
    private static final String KEY_ID = "archcore-enc-key";

    private JwksClient jwksClient;

    @Override
    public AccessTokenResponse transformAccessTokenResponse(
            AccessTokenResponse accessTokenResponse,
            ProtocolMapperModel mappingModel,
            KeycloakSession session,
            UserSessionModel userSession,
            ClientSessionContext clientSessionCtx) {

        String targetClientId = getConfigValue(mappingModel, TARGET_CLIENT_CONFIG, DEFAULT_TARGET_CLIENT);
        String clientId = clientSessionCtx.getClientSession().getClient().getClientId();

        if (!targetClientId.equals(clientId)) {
            logger.debug("Skipping JWE encryption for client: {} (target: {})", clientId, targetClientId);
            return accessTokenResponse;
        }

        logger.info("Encrypting access token to JWE for client: {}", clientId);

        try {
            String signedToken = accessTokenResponse.getToken();
            if (signedToken == null || signedToken.isEmpty()) {
                logger.warn("No access token to encrypt for client: {}", clientId);
                return accessTokenResponse;
            }

            String jweToken = encryptToJwe(signedToken, mappingModel);
            accessTokenResponse.setToken(jweToken);
            logger.info("Access token encrypted successfully for client: {}", clientId);
        } catch (Exception e) {
            logger.error("Failed to encrypt access token for client: {}", clientId, e);
            throw new RuntimeException("JWE encryption failed", e);
        }

        return accessTokenResponse;
    }

    String encryptToJwe(String signedToken, ProtocolMapperModel mappingModel) throws Exception {
        RSAPublicKey publicKey = getJwksClient(mappingModel).getPublicKey();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .keyID(KEY_ID)
                .algorithm(JWEAlgorithm.RSA_OAEP_256)
                .build();

        JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
                .keyID(KEY_ID)
                .build();

        JWEObject jweObject = new JWEObject(header, new Payload(signedToken));
        jweObject.encrypt(new RSAEncrypter(rsaKey));

        return jweObject.serialize();
    }

    private JwksClient getJwksClient(ProtocolMapperModel mappingModel) {
        if (jwksClient == null) {
            String jwksUrl = getConfigValue(mappingModel, JWKS_URL_CONFIG, DEFAULT_JWKS_URL);
            long cacheTtlMs = Long.parseLong(
                    getConfigValue(mappingModel, CACHE_TTL_MS_CONFIG, String.valueOf(DEFAULT_CACHE_TTL_MS)));
            jwksClient = new JwksClient(jwksUrl, cacheTtlMs);
        }
        return jwksClient;
    }

    private String getConfigValue(ProtocolMapperModel mappingModel, String key, String defaultValue) {
        String value = mappingModel.getConfig().get(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    public String getProviderId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayCategory() {
        return "Token Response Mapper";
    }

    @Override
    public String getDisplayType() {
        return "ArchCore JWE Access Token Response Encryptor";
    }

    @Override
    public String getHelpText() {
        return "Encrypts the access token in the token response to JWE (RSA-OAEP-256 / A256GCM) for a specific client.";
    }

    public List<ProviderConfigProperty> getConfigProperties() {
        ProviderConfigProperty targetClient = new ProviderConfigProperty();
        targetClient.setName(TARGET_CLIENT_CONFIG);
        targetClient.setLabel("Target Client ID");
        targetClient.setHelpText("The client ID for which access tokens should be encrypted to JWE.");
        targetClient.setType(ProviderConfigProperty.STRING_TYPE);
        targetClient.setDefaultValue(DEFAULT_TARGET_CLIENT);
        targetClient.setSecret(false);

        ProviderConfigProperty jwksUrl = new ProviderConfigProperty();
        jwksUrl.setName(JWKS_URL_CONFIG);
        jwksUrl.setLabel("JWKS URL");
        jwksUrl.setHelpText("The URL to fetch the Resource Server's public RSA key for encryption.");
        jwksUrl.setType(ProviderConfigProperty.STRING_TYPE);
        jwksUrl.setDefaultValue(DEFAULT_JWKS_URL);
        jwksUrl.setSecret(false);

        ProviderConfigProperty cacheTtl = new ProviderConfigProperty();
        cacheTtl.setName(CACHE_TTL_MS_CONFIG);
        cacheTtl.setLabel("Cache TTL (ms)");
        cacheTtl.setHelpText("Cache time-to-live in milliseconds for the JWKS public key.");
        cacheTtl.setType(ProviderConfigProperty.STRING_TYPE);
        cacheTtl.setDefaultValue(String.valueOf(DEFAULT_CACHE_TTL_MS));
        cacheTtl.setSecret(false);

        return List.of(targetClient, jwksUrl, cacheTtl);
    }

    public static ProtocolMapperModel createMapper(String targetClientId, String jwksUrl, long cacheTtlMs) {
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName("archcore-jwe-access-token-response");
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol("openid-connect");
        Map<String, String> config = new HashMap<>();
        config.put(TARGET_CLIENT_CONFIG, targetClientId);
        config.put(JWKS_URL_CONFIG, jwksUrl);
        config.put(CACHE_TTL_MS_CONFIG, String.valueOf(cacheTtlMs));
        mapper.setConfig(config);
        return mapper;
    }
}
