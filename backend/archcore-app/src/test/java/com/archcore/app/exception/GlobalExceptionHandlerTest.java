package com.archcore.app.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/test");
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with field errors")
    void shouldHandleValidationException() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("request", "email", "Must be a valid email");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                (MethodParameter) null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation Failed", response.getBody().error());
        assertEquals(1, response.getBody().fieldErrors().size());
        assertEquals("email", response.getBody().fieldErrors().getFirst().field());
    }

    @Test
    @DisplayName("Should handle AccessDeniedException")
    void shouldHandleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Forbidden", response.getBody().error());
        assertEquals("You do not have permission to access this resource", response.getBody().message());
    }

    @Test
    @DisplayName("Should handle AuthenticationException")
    void shouldHandleAuthenticationException() {
        AuthenticationException ex = mock(AuthenticationException.class);
        when(ex.getMessage()).thenReturn("Bad credentials");

        ResponseEntity<ErrorResponse> response = handler.handleAuthentication(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unauthorized", response.getBody().error());
        assertEquals("Authentication required", response.getBody().message());
    }

    @Test
    @DisplayName("Should handle BadCredentialsException")
    void shouldHandleBadCredentials() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<ErrorResponse> response = handler.handleAuthentication(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException")
    void shouldHandleResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User", "id", "123");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Not Found", response.getBody().error());
        assertTrue(response.getBody().message().contains("User"));
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException")
    void shouldHandleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("Invalid input", response.getBody().message());
    }

    @Test
    @DisplayName("Should handle generic Exception with traceId")
    void shouldHandleGenericException() {
        Exception ex = new RuntimeException("Something went wrong");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal Server Error", response.getBody().error());
        assertTrue(response.getBody().message().contains("Reference:"));
    }

    @Test
    @DisplayName("ErrorResponse should not include null fieldErrors in JSON")
    void errorResponseShouldExcludeNullFieldErrors() {
        ErrorResponse response = new ErrorResponse(
                500, "Error", "msg", "/path", java.time.Instant.now(), null);

        assertNull(response.fieldErrors());
    }

    @Test
    @DisplayName("ErrorResponse FieldError should hold field and message")
    void fieldErrorShouldHoldData() {
        ErrorResponse.FieldError fe = new ErrorResponse.FieldError("email", "invalid");

        assertEquals("email", fe.field());
        assertEquals("invalid", fe.message());
    }
}
