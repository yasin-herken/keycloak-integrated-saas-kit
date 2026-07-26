package com.archcore.keycloak.spi.jwe;

import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.jwk.RSAKey;
import org.keycloak.Config;
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

    private static volatile JweProviderConfig providerConfig;
    private JwksClient jwksClient;

    @Override
    public void init(Config.Scope config) {
        super.init(config);
        providerConfig = JweProviderConfig.fromScope(config);
    }

    @Override
    public AccessTokenResponse transformAccessTokenResponse(
            AccessTokenResponse accessTokenResponse,
            ProtocolMapperModel mappingModel,
            KeycloakSession session,
            UserSessionModel userSession,
            ClientSessionContext clientSessionCtx) {

        String targetClientId = getConfigValue(mappingModel, TARGET_CLIENT_CONFIG, null);
        String clientId = clientSessionCtx.getClientSession().getClient().getClientId();

        if (!clientId.equals(targetClientId)) {
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
        JweProviderConfig config = getProviderConfig();
        RSAPublicKey publicKey = getJwksClient(mappingModel).getPublicKey();

        JWEAlgorithm algorithm = JWEAlgorithm.parse(config.getJweAlgorithm());
        EncryptionMethod encryptionMethod = EncryptionMethod.parse(config.getEncryptionMethod());

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .keyID(config.getKeyId())
                .algorithm(algorithm)
                .build();

        JWEHeader header = new JWEHeader.Builder(algorithm, encryptionMethod)
                .keyID(config.getKeyId())
                .build();

        JWEObject jweObject = new JWEObject(header, new Payload(signedToken));
        jweObject.encrypt(new RSAEncrypter(rsaKey));

        return jweObject.serialize();
    }

    private JwksClient getJwksClient(ProtocolMapperModel mappingModel) {
        if (jwksClient == null) {
            JweProviderConfig config = getProviderConfig();
            String jwksUrl = getConfigValue(mappingModel, JWKS_URL_CONFIG, config.getDefaultJwksUrl());
            long cacheTtlMs = Long.parseLong(
                    getConfigValue(mappingModel, CACHE_TTL_MS_CONFIG, String.valueOf(config.getDefaultCacheTtlMs())));
            jwksClient = new JwksClient(jwksUrl, cacheTtlMs);
        }
        return jwksClient;
    }

    private static JweProviderConfig getProviderConfig() {
        if (providerConfig == null) {
            providerConfig = JweProviderConfig.fromScope(null);
        }
        return providerConfig;
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
        return "Encrypts the access token in the token response to JWE (RSA-OAEP / A256GCM) for a specific client.";
    }

    public List<ProviderConfigProperty> getConfigProperties() {
        JweProviderConfig config = getProviderConfig();

        ProviderConfigProperty targetClient = new ProviderConfigProperty();
        targetClient.setName(TARGET_CLIENT_CONFIG);
        targetClient.setLabel("Target Client ID");
        targetClient.setHelpText("The client ID for which access tokens should be encrypted to JWE.");
        targetClient.setType(ProviderConfigProperty.STRING_TYPE);
        targetClient.setSecret(false);

        ProviderConfigProperty jwksUrl = new ProviderConfigProperty();
        jwksUrl.setName(JWKS_URL_CONFIG);
        jwksUrl.setLabel("JWKS URL");
        jwksUrl.setHelpText("The URL to fetch the Resource Server's public RSA key for encryption. Defaults to provider config value.");
        jwksUrl.setType(ProviderConfigProperty.STRING_TYPE);
        jwksUrl.setDefaultValue(config.getDefaultJwksUrl());
        jwksUrl.setSecret(false);

        ProviderConfigProperty cacheTtl = new ProviderConfigProperty();
        cacheTtl.setName(CACHE_TTL_MS_CONFIG);
        cacheTtl.setLabel("Cache TTL (ms)");
        cacheTtl.setHelpText("Cache time-to-live in milliseconds for the JWKS public key. Defaults to provider config value.");
        cacheTtl.setType(ProviderConfigProperty.STRING_TYPE);
        cacheTtl.setDefaultValue(String.valueOf(config.getDefaultCacheTtlMs()));
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
