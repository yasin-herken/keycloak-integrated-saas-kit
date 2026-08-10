package com.archcore.app.controller;

import com.archcore.app.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static com.archcore.app.config.TestSecurityConfig.TEST_TOKEN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = true)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class SecurityHeadersIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should include X-Content-Type-Options header")
    void shouldIncludeXContentTypeOptions() throws Exception {
        mockMvc.perform(get("/api/v1/test-endpoint")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    @DisplayName("Should include X-Frame-Options header")
    void shouldIncludeXFrameOptions() throws Exception {
        mockMvc.perform(get("/api/v1/test-endpoint")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN))
            .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    @DisplayName("Should include Content-Security-Policy header")
    void shouldIncludeCSP() throws Exception {
        mockMvc.perform(get("/api/v1/test-endpoint")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN))
            .andExpect(header().exists("Content-Security-Policy"))
            .andExpect(header().string("Content-Security-Policy",
                org.hamcrest.Matchers.containsString("default-src 'self'")));
    }

    @Test
    @DisplayName("Should include Referrer-Policy header")
    void shouldIncludeReferrerPolicy() throws Exception {
        mockMvc.perform(get("/api/v1/test-endpoint")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN))
            .andExpect(header().exists("Referrer-Policy"));
    }

    @Test
    @DisplayName("Should include Permissions-Policy header")
    void shouldIncludePermissionsPolicy() throws Exception {
        mockMvc.perform(get("/api/v1/test-endpoint")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN))
            .andExpect(header().exists("Permissions-Policy"));
    }

    @Test
    @DisplayName("Should include X-XSS-Protection header")
    void shouldIncludeXssProtection() throws Exception {
        mockMvc.perform(get("/api/v1/test-endpoint")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN))
            .andExpect(header().exists("X-XSS-Protection"));
    }

    @Test
    @DisplayName("CORS - should respond to OPTIONS preflight")
    void shouldHandleCorsPreflight() throws Exception {
        mockMvc.perform(options("/api/v1/test-endpoint")
                .header("Origin", "https://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "Authorization"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CORS - should include allowed origins header")
    void shouldIncludeCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/v1/test-endpoint")
                .header("Origin", "https://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
            .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("CSRF - should be disabled for stateless API")
    void shouldNotRequireCsrfToken() throws Exception {
        mockMvc.perform(get("/api/v1/test-endpoint")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Protected endpoint - should return 401 without token")
    void shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/test-endpoint"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CSP should block frame ancestors")
    void cspShouldBlockFrameAncestors() throws Exception {
        mockMvc.perform(get("/api/v1/test-endpoint")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN))
            .andExpect(header().string("Content-Security-Policy",
                org.hamcrest.Matchers.containsString("frame-ancestors 'none'")));
    }

    @Test
    @DisplayName("CSP should restrict script sources")
    void cspShouldRestrictScripts() throws Exception {
        mockMvc.perform(get("/api/v1/test-endpoint")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN))
            .andExpect(header().string("Content-Security-Policy",
                org.hamcrest.Matchers.containsString("script-src 'self'")));
    }
}
