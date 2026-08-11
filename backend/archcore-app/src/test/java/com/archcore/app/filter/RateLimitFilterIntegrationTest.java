package com.archcore.app.filter;

import com.archcore.app.config.Bucket4jConfig;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = true)
@Import({Bucket4jConfig.class, TestSecurityConfig.class})
@ActiveProfiles("test")
class RateLimitFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Request should pass through rate limit filter when disabled")
    void shouldPassThroughWhenRateLimitDisabled() throws Exception {
        mockMvc.perform(get("/api/v1/test-endpoint")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Multiple requests should not be rate limited when disabled")
    void shouldNotRateLimitMultipleRequests() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/v1/test-endpoint")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk());
        }
    }
}
