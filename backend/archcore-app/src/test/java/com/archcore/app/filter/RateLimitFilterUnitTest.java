package com.archcore.app.filter;

import com.archcore.app.config.RateLimitProperties;
import com.archcore.app.config.RateLimitProperties.KeyStrategy;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.caffeine.Bucket4jCaffeine;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitFilterUnitTest {

    private RateLimitFilter filter;
    private RateLimitFilter compositeFilter;
    private RateLimitFilter userIdFilter;
    private RateLimitFilter ipFilter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        @SuppressWarnings("unchecked")
        ProxyManager<String> proxyManager = (ProxyManager<String>) (ProxyManager<?>) Bucket4jCaffeine.builderFor(
                (Caffeine<?, ?>) Caffeine.newBuilder().maximumSize(10_000)
        ).build();

        RateLimitProperties compositeProps = new RateLimitProperties(true, KeyStrategy.COMPOSITE, List.of(
                new RateLimitProperties.FilterConfig("/api/v1/auth/.*", 3, 3, 60),
                new RateLimitProperties.FilterConfig("/api/v1/.*", 300, 300, 60)
        ));
        compositeFilter = new RateLimitFilter(proxyManager, compositeProps, objectMapper);

        RateLimitProperties ipProps = new RateLimitProperties(true, KeyStrategy.IP, List.of(
                new RateLimitProperties.FilterConfig("/api/v1/auth/.*", 3, 3, 60)
        ));
        ipFilter = new RateLimitFilter(proxyManager, ipProps, objectMapper);

        RateLimitProperties userIdProps = new RateLimitProperties(true, KeyStrategy.USER_ID, List.of(
                new RateLimitProperties.FilterConfig("/api/v1/auth/.*", 3, 3, 60)
        ));
        userIdFilter = new RateLimitFilter(proxyManager, userIdProps, objectMapper);

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("COMPOSITE: Same IP, different users should have separate rate limits")
    void compositeShouldSeparateByUserId() throws Exception {
        Jwt jwt1 = Jwt.withTokenValue("token1")
                .subject("user-123")
                .header("alg", "RS256")
                .claim("iss", "http://localhost:8080/realms/test")
                .build();
        Jwt jwt2 = Jwt.withTokenValue("token2")
                .subject("user-456")
                .header("alg", "RS256")
                .claim("iss", "http://localhost:8080/realms/test")
                .build();

        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest request = createRequest("/api/v1/auth/login");
            request.addHeader("X-Forwarded-For", "10.0.0.1");
            MockHttpServletResponse response = new MockHttpServletResponse();
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(jwt1, null));
            compositeFilter.doFilterInternal(request, response, (req, res) -> {});
        }

        MockHttpServletRequest request = createRequest("/api/v1/auth/login");
        request.addHeader("X-Forwarded-For", "10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(jwt2, null));
        compositeFilter.doFilterInternal(request, response, (req, res) -> {});

        assertEquals(200, response.getStatus(), "Different user from same IP should have separate bucket");
    }

    @Test
    @DisplayName("COMPOSITE: Same user should share rate limit across different IPs")
    void compositeShouldShareByUserId() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token")
                .subject("user-123")
                .header("alg", "RS256")
                .claim("iss", "http://localhost:8080/realms/test")
                .build();

        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest request = createRequest("/api/v1/auth/login");
            request.addHeader("X-Forwarded-For", "10.0.0." + (i + 1));
            MockHttpServletResponse response = new MockHttpServletResponse();
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(jwt, null));
            compositeFilter.doFilterInternal(request, response, (req, res) -> {});
        }

        MockHttpServletRequest request = createRequest("/api/v1/auth/login");
        request.addHeader("X-Forwarded-For", "10.0.0.99");
        MockHttpServletResponse response = new MockHttpServletResponse();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(jwt, null));
        compositeFilter.doFilterInternal(request, response, (req, res) -> {});

        assertEquals(429, response.getStatus(), "Same user from different IPs should share bucket");
    }

    @Test
    @DisplayName("COMPOSITE: API key should separate rate limits")
    void compositeShouldSeparateByApiKey() throws Exception {
        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest request = createRequest("/api/v1/auth/login");
            request.addHeader("X-Forwarded-For", "10.0.0.1");
            request.addHeader("X-API-Key", "key-aaa");
            MockHttpServletResponse response = new MockHttpServletResponse();
            compositeFilter.doFilterInternal(request, response, (req, res) -> {});
        }

        MockHttpServletRequest request = createRequest("/api/v1/auth/login");
        request.addHeader("X-Forwarded-For", "10.0.0.1");
        request.addHeader("X-API-Key", "key-bbb");
        MockHttpServletResponse response = new MockHttpServletResponse();
        compositeFilter.doFilterInternal(request, response, (req, res) -> {});

        assertEquals(200, response.getStatus(), "Different API key should have separate bucket");
    }

    @Test
    @DisplayName("IP strategy: All requests from same IP share bucket")
    void ipStrategyShouldShareByIp() throws Exception {
        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest request = createRequest("/api/v1/auth/login");
            request.addHeader("X-Forwarded-For", "10.0.0.1");
            MockHttpServletResponse response = new MockHttpServletResponse();
            ipFilter.doFilterInternal(request, response, (req, res) -> {});
        }

        MockHttpServletRequest request = createRequest("/api/v1/auth/login");
        request.addHeader("X-Forwarded-For", "10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ipFilter.doFilterInternal(request, response, (req, res) -> {});

        assertEquals(429, response.getStatus(), "Same IP should share bucket");
    }

    @Test
    @DisplayName("USER_ID strategy: Authenticated user gets rate limited by user ID")
    void userIdStrategyShouldLimitByUser() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token")
                .subject("user-789")
                .header("alg", "RS256")
                .claim("iss", "http://localhost:8080/realms/test")
                .build();

        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest request = createRequest("/api/v1/auth/login");
            MockHttpServletResponse response = new MockHttpServletResponse();
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(jwt, null));
            userIdFilter.doFilterInternal(request, response, (req, res) -> {});
        }

        MockHttpServletRequest request = createRequest("/api/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(jwt, null));
        userIdFilter.doFilterInternal(request, response, (req, res) -> {});

        assertEquals(429, response.getStatus(), "Same user should share bucket");
    }

    @Test
    @DisplayName("Should return 429 with strategy in error response")
    void shouldReturnStrategyInErrorResponse() throws Exception {
        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest request = createRequest("/api/v1/auth/login");
            MockHttpServletResponse response = new MockHttpServletResponse();
            compositeFilter.doFilterInternal(request, response, (req, res) -> {});
        }

        MockHttpServletRequest request = createRequest("/api/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        compositeFilter.doFilterInternal(request, response, (req, res) -> {});

        assertEquals(429, response.getStatus());
        String body = response.getContentAsString();
        assertTrue(body.contains("COMPOSITE"), "Error response should include strategy");
        assertTrue(body.contains("retryAfterSeconds"), "Error response should include retryAfterSeconds");
    }

    @Test
    @DisplayName("Should include X-Rate-Limit-Key-Strategy header")
    void shouldIncludeStrategyHeader() throws Exception {
        MockHttpServletRequest request = createRequest("/api/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        compositeFilter.doFilterInternal(request, response, (req, res) -> {});

        assertEquals(200, response.getStatus());
        assertEquals("COMPOSITE", response.getHeader("X-Rate-Limit-Key-Strategy"));
    }

    @Test
    @DisplayName("Should extract client IP from multiple proxy headers")
    void shouldExtractClientIpFromProxyHeaders() throws Exception {
        MockHttpServletRequest request = createRequest("/api/v1/auth/login");
        request.addHeader("X-Forwarded-For", "10.0.0.1, 10.0.0.2, 10.0.0.3");
        MockHttpServletResponse response = new MockHttpServletResponse();
        compositeFilter.doFilterInternal(request, response, (req, res) -> {});

        assertEquals(200, response.getStatus());
    }

    private MockHttpServletRequest createRequest(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRemoteAddr("192.168.1.1");
        return request;
    }
}
