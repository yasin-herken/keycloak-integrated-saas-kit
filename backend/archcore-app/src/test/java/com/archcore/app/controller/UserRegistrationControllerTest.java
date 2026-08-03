package com.archcore.app.controller;

import com.archcore.app.dto.UserRegistrationRequest;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserRegistrationController.class)
@ActiveProfiles("test")
class UserRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRegistrationRequest createValidRequest() {
        return new UserRegistrationRequest("john_doe", "john@example.com", "Secure@123");
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - valid request returns 201")
    @WithMockUser
    void shouldReturn201ForValidRequest() throws Exception {
        UserRegistrationRequest request = createValidRequest();

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("john_doe"))
            .andExpect(jsonPath("$.email").value("john@example.com"))
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - missing username returns 400")
    @WithMockUser
    void shouldReturn400WhenUsernameMissing() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest(null, "john@example.com", "Secure@123");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation Failed"))
            .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - invalid email returns 400")
    @WithMockUser
    void shouldReturn400WhenEmailInvalid() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest("john_doe", "not-an-email", "Secure@123");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - weak password returns 400")
    @WithMockUser
    void shouldReturn400WhenPasswordWeak() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest("john_doe", "john@example.com", "weak");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - empty body returns 400")
    @WithMockUser
    void shouldReturn400WhenBodyEmpty() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - no auth returns 401/403")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        UserRegistrationRequest request = createValidRequest();

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - XSS attempt in username is rejected")
    @WithMockUser
    void shouldRejectXssInUsername() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest("<script>alert('xss')</script>", "john@example.com", "Secure@123");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - SQL injection attempt in username is rejected")
    @WithMockUser
    void shouldRejectSqlInjectionInUsername() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest("'; DROP TABLE users; --", "john@example.com", "Secure@123");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - wrong content type returns 415")
    @WithMockUser
    void shouldReturn415ForWrongContentType() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.TEXT_PLAIN)
                .content("test"))
            .andExpect(status().isUnsupportedMediaType());
    }
}
