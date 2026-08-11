package com.archcore.app.controller;

import com.archcore.app.exception.ErrorResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
public class ErrorController implements org.springframework.boot.webmvc.error.ErrorController {

    private static final Logger log = LoggerFactory.getLogger(ErrorController.class);

    private static final Map<Integer, String> ERROR_MESSAGES = Map.of(
        400, "Bad Request",
        401, "Authentication required",
        403, "Access denied",
        404, "Resource not found",
        405, "Method not allowed",
        500, "Internal Server Error"
    );

    @RequestMapping("/error")
    public ResponseEntity<ErrorResponse> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Throwable error = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        int status = statusCode != null ? statusCode : HttpStatus.INTERNAL_SERVER_ERROR.value();
        String uri = requestUri != null ? requestUri : request.getRequestURI();
        String message = ERROR_MESSAGES.getOrDefault(status, "An error occurred");

        if (status >= 500) {
            String traceId = UUID.randomUUID().toString();
            log.error("Server error [traceId={}] on {}: {}", traceId, uri,
                error != null ? error.getMessage() : "unknown", error);
            message = "An unexpected error occurred. Reference: " + traceId;
        } else {
            log.warn("Client error {} on {}: {}", status, uri,
                error != null ? error.getMessage() : "unknown");
        }

        ErrorResponse response = new ErrorResponse(
            status,
            HttpStatus.valueOf(status).getReasonPhrase(),
            message,
            uri,
            Instant.now(),
            null
        );

        return ResponseEntity.status(status).body(response);
    }
}
