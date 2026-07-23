package com.archcore.keycloak.spi.jwe;

import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.representations.AccessTokenResponse;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JweAccessTokenResponseMapperTest {

    private static RSAPublicKey publicKey;
    private static RSAPrivateKey privateKey;

    @Mock
    private KeycloakSession keycloakSession;

    @Mock
    private UserSessionModel userSession;

    @Mock
    private ClientSessionContext clientSessionCtx;

    @Mock
    private AuthenticatedClientSessionModel clientSession;

    @Mock
    private ClientModel clientModel;

    private JweAccessTokenResponseMapper mapper;
    private ProtocolMapperModel mappingModel;

    @BeforeAll
    static void generateKeys() throws Exception {
        RSAKey rsaKeyPair = new RSAKeyGenerator(2048)
                .keyID("archcore-enc-key")
                .algorithm(JWEAlgorithm.RSA_OAEP_256)
                .generate();
        publicKey = rsaKeyPair.toRSAPublicKey();
        privateKey = rsaKeyPair.toRSAPrivateKey();
    }

    @BeforeEach
    void setUp() {
        mapper = new JweAccessTokenResponseMapper();
        mappingModel = new ProtocolMapperModel();
        Map<String, String> config = new HashMap<>();
        config.put(JweAccessTokenResponseMapper.TARGET_CLIENT_CONFIG, "archcore-client");
        config.put(JweAccessTokenResponseMapper.JWKS_URL_CONFIG, "http://localhost:8081/.well-known/jwks.json");
        config.put(JweAccessTokenResponseMapper.CACHE_TTL_MS_CONFIG, "300000");
        mappingModel.setConfig(config);
    }

    @Test
    void transformAccessTokenResponse_shouldEncryptTokenForTargetClient() throws Exception {
        when(clientSessionCtx.getClientSession()).thenReturn(clientSession);
        when(clientSession.getClient()).thenReturn(clientModel);
        when(clientModel.getClientId()).thenReturn("archcore-client");

        AccessTokenResponse response = new AccessTokenResponse();
        response.setToken("eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1c2VyLTEyMyJ9.signature");

        injectMockJwksClient();

        AccessTokenResponse result = mapper.transformAccessTokenResponse(
                response, mappingModel, keycloakSession, userSession, clientSessionCtx);

        assertNotNull(result.getToken());
        String[] parts = result.getToken().split("\\.");
        assertEquals(5, parts.length, "JWE token must have 5 parts");

        JWEObject jweObject = JWEObject.parse(result.getToken());
        assertEquals(JWEAlgorithm.RSA_OAEP_256, jweObject.getHeader().getAlgorithm());

        jweObject.decrypt(new RSADecrypter(privateKey));
        assertEquals("eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1c2VyLTEyMyJ9.signature",
                jweObject.getPayload().toString());
    }

    @Test
    void transformAccessTokenResponse_shouldSkipForNonTargetClient() throws Exception {
        when(clientSessionCtx.getClientSession()).thenReturn(clientSession);
        when(clientSession.getClient()).thenReturn(clientModel);
        when(clientModel.getClientId()).thenReturn("other-client");

        AccessTokenResponse response = new AccessTokenResponse();
        response.setToken("original-token");

        AccessTokenResponse result = mapper.transformAccessTokenResponse(
                response, mappingModel, keycloakSession, userSession, clientSessionCtx);

        assertEquals("original-token", result.getToken());
    }

    @Test
    void transformAccessTokenResponse_shouldHandleNullToken() throws Exception {
        when(clientSessionCtx.getClientSession()).thenReturn(clientSession);
        when(clientSession.getClient()).thenReturn(clientModel);
        when(clientModel.getClientId()).thenReturn("archcore-client");

        AccessTokenResponse response = new AccessTokenResponse();
        response.setToken(null);

        AccessTokenResponse result = mapper.transformAccessTokenResponse(
                response, mappingModel, keycloakSession, userSession, clientSessionCtx);

        assertNull(result.getToken());
    }

    @Test
    void transformAccessTokenResponse_shouldHandleEmptyToken() throws Exception {
        when(clientSessionCtx.getClientSession()).thenReturn(clientSession);
        when(clientSession.getClient()).thenReturn(clientModel);
        when(clientModel.getClientId()).thenReturn("archcore-client");

        AccessTokenResponse response = new AccessTokenResponse();
        response.setToken("");

        AccessTokenResponse result = mapper.transformAccessTokenResponse(
                response, mappingModel, keycloakSession, userSession, clientSessionCtx);

        assertEquals("", result.getToken());
    }

    @Test
    void getProviderId_shouldReturnCorrectId() {
        assertEquals("archcore-jwe-access-token-response-mapper", mapper.getProviderId());
    }

    @Test
    void getId_shouldReturnCorrectId() {
        assertEquals("archcore-jwe-access-token-response-mapper", mapper.getId());
    }

    @Test
    void getDisplayType_shouldReturnCorrectName() {
        assertEquals("ArchCore JWE Access Token Response Encryptor", mapper.getDisplayType());
    }

    @Test
    void getConfigProperties_shouldReturnThreeProperties() {
        assertEquals(3, mapper.getConfigProperties().size());
    }

    @Test
    void encryptToJwe_shouldProduceValidFivePartJwe() throws Exception {
        injectMockJwksClient();
        String signedToken = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ0ZXN0LXVzZXIifQ.signature";

        String jweToken = mapper.encryptToJwe(signedToken, mappingModel);

        String[] parts = jweToken.split("\\.");
        assertEquals(5, parts.length, "JWE token must have 5 parts");

        JWEObject jweObject = JWEObject.parse(jweToken);
        assertEquals(JWEAlgorithm.RSA_OAEP_256, jweObject.getHeader().getAlgorithm());
        assertEquals("archcore-enc-key", jweObject.getHeader().getKeyID());

        jweObject.decrypt(new RSADecrypter(privateKey));
        assertEquals(signedToken, jweObject.getPayload().toString());
    }

    @Test
    void createMapper_shouldCreateMapperWithCorrectConfig() {
        ProtocolMapperModel result = JweAccessTokenResponseMapper.createMapper(
                "test-client", "http://example.com/jwks", 60000);

        assertEquals("archcore-jwe-access-token-response-mapper", result.getProtocolMapper());
        assertEquals("openid-connect", result.getProtocol());
        assertEquals("test-client", result.getConfig().get(JweAccessTokenResponseMapper.TARGET_CLIENT_CONFIG));
        assertEquals("http://example.com/jwks", result.getConfig().get(JweAccessTokenResponseMapper.JWKS_URL_CONFIG));
        assertEquals("60000", result.getConfig().get(JweAccessTokenResponseMapper.CACHE_TTL_MS_CONFIG));
    }

    private void injectMockJwksClient() {
        JwksClient mockClient = mock(JwksClient.class);
        when(mockClient.getPublicKey()).thenReturn(publicKey);

        try {
            var field = JweAccessTokenResponseMapper.class.getDeclaredField("jwksClient");
            field.setAccessible(true);
            field.set(mapper, mockClient);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
