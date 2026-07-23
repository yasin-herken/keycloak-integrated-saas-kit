package com.archcore.keycloak.spi.jwe;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.interfaces.RSAPublicKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class JwksClientTest {

    private static final String JWKS_URL = "http://host.docker.internal:8081/.well-known/jwks.json";
    private static final long CACHE_TTL_MS = 5 * 60 * 1000L;

    private RSAPublicKey expectedPublicKey;

    @SuppressWarnings("unchecked")
    private HttpClient mockHttpClient;
    @SuppressWarnings("unchecked")
    private HttpResponse mockResponse;

    private JwksClient jwksClient;

    @BeforeEach
    void setUp() throws Exception {
        RSAKey rsaKeyPair = new RSAKeyGenerator(2048)
                .keyID("test-key")
                .generate();
        expectedPublicKey = rsaKeyPair.toRSAPublicKey();

        mockHttpClient = mock(HttpClient.class);
        mockResponse = mock(HttpResponse.class);

        JWKSet jwkSet = new JWKSet(rsaKeyPair);
        lenient().when(mockResponse.statusCode()).thenReturn(200);
        lenient().when(mockResponse.body()).thenReturn(jwkSet.toString());

        jwksClient = new JwksClient(JWKS_URL, CACHE_TTL_MS, mockHttpClient);
    }

    @Test
    void getPublicKey_shouldFetchAndReturnPublicKey() throws Exception {
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        RSAPublicKey publicKey = jwksClient.getPublicKey();

        assertNotNull(publicKey);
        assertEquals(expectedPublicKey.getPublicExponent(), publicKey.getPublicExponent());
        assertEquals(expectedPublicKey.getModulus(), publicKey.getModulus());
    }

    @Test
    void getPublicKey_shouldCacheAndReturnCachedKey() throws Exception {
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        RSAPublicKey first = jwksClient.getPublicKey();
        RSAPublicKey second = jwksClient.getPublicKey();

        assertSame(first, second);
        verify(mockHttpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void getPublicKey_shouldReturnStaleCacheOnFailure() throws Exception {
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse)
                .thenThrow(new RuntimeException("Network error"));

        RSAPublicKey first = jwksClient.getPublicKey();
        RSAPublicKey second = jwksClient.getPublicKey();

        assertNotNull(first);
        assertNotNull(second);
        assertEquals(expectedPublicKey, second);
    }

    @Test
    void getPublicKey_shouldThrowOnNon200Status() throws Exception {
        HttpResponse<String> errorResponse = mock(HttpResponse.class);
        lenient().when(errorResponse.statusCode()).thenReturn(404);
        lenient().when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(errorResponse);

        JwksClient freshClient = new JwksClient(JWKS_URL, CACHE_TTL_MS, mockHttpClient);
        assertThrows(RuntimeException.class, () -> freshClient.getPublicKey());
    }

    @Test
    void getPublicKey_shouldThrowOnNoKeysInJwks() throws Exception {
        JWKSet emptyJwkSet = new JWKSet();
        when(mockResponse.body()).thenReturn(emptyJwkSet.toString());
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        assertThrows(RuntimeException.class, () -> jwksClient.getPublicKey());
    }
}
