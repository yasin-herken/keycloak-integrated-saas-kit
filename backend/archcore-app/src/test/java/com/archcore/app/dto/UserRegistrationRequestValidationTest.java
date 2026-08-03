package com.archcore.app.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserRegistrationRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private UserRegistrationRequest createValidRequest() {
        return new UserRegistrationRequest("john_doe", "john@example.com", "Secure@123");
    }

    @Nested
    @DisplayName("Username Validation")
    class UsernameValidation {

        @Test
        @DisplayName("Should accept valid username")
        void shouldAcceptValidUsername() {
            UserRegistrationRequest request = new UserRegistrationRequest("john_doe", "john@example.com", "Secure@123");
            Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should reject blank username")
        void shouldRejectBlankUsername() {
            UserRegistrationRequest request = new UserRegistrationRequest("", "john@example.com", "Secure@123");
            Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
        }

        @Test
        @DisplayName("Should reject username shorter than 3 characters")
        void shouldRejectShortUsername() {
            UserRegistrationRequest request = new UserRegistrationRequest("ab", "john@example.com", "Secure@123");
            Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should reject username longer than 50 characters")
        void shouldRejectLongUsername() {
            String longUsername = "a".repeat(51);
            UserRegistrationRequest request = new UserRegistrationRequest(longUsername, "john@example.com", "Secure@123");
            Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should reject username with special characters")
        void shouldRejectUsernameWithSpecialChars() {
            UserRegistrationRequest request = new UserRegistrationRequest("john@doe!", "john@example.com", "Secure@123");
            Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should accept username with hyphens and underscores")
        void shouldAcceptUsernameWithHyphensUnderscores() {
            UserRegistrationRequest request = new UserRegistrationRequest("john-doe_123", "john@example.com", "Secure@123");
            Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("Email Validation")
    class EmailValidation {

        @Test
        @DisplayName("Should accept valid email")
        void shouldAcceptValidEmail() {
            UserRegistrationRequest request = createValidRequest();
            Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should reject blank email")
        void shouldRejectBlankEmail() {
            UserRegistrationRequest request = new UserRegistrationRequest("john_doe", "", "Secure@123");
            Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should reject invalid email format")
        void shouldRejectInvalidEmail() {
            UserRegistrationRequest request = new UserRegistrationRequest("john_doe", "not-an-email", "Secure@123");
            Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should reject email exceeding 255 characters")
        void shouldRejectLongEmail() {
            String longEmail = "a".repeat(246) + "@example.com"; // 256 chars
            UserRegistrationRequest request = new UserRegistrationRequest("john_doe", longEmail, "Secure@123");
            Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
            assertFalse(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("Password Validation")
    class PasswordValidation {

        @Test
        @DisplayName("Should accept valid password")
        void shouldAcceptValidPassword() {
            UserRegistrationRequest request = createValidRequest();
            Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should reject blank password")
        void shouldRejectBlankPassword() {
            UserRegistrationRequest request = new UserRegistrationRequest("john_doe", "john@example.com", "");
            Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should reject password shorter than 8 characters")
        void shouldRejectShortPassword() {
            UserRegistrationRequest request = new UserRegistrationRequest("john_doe", "john@example.com", "Ab@1");
            Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should reject password without uppercase")
        void shouldRejectPasswordWithoutUppercase() {
            UserRegistrationRequest request = new UserRegistrationRequest("john_doe", "john@example.com", "secure@123");
            Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should reject password without lowercase")
        void shouldRejectPasswordWithoutLowercase() {
            UserRegistrationRequest request = new UserRegistrationRequest("john_doe", "john@example.com", "SECURE@123");
            Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should reject password without digit")
        void shouldRejectPasswordWithoutDigit() {
            UserRegistrationRequest request = new UserRegistrationRequest("john_doe", "john@example.com", "Secure@abc");
            Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should reject password without special character")
        void shouldRejectPasswordWithoutSpecialChar() {
            UserRegistrationRequest request = new UserRegistrationRequest("john_doe", "john@example.com", "Secure123");
            Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should reject password exceeding 128 characters")
        void shouldRejectLongPassword() {
            String longPassword = "A".repeat(122) + "@1abcde"; // 129 chars
            UserRegistrationRequest request = new UserRegistrationRequest("john_doe", "john@example.com", longPassword);
            Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
            assertFalse(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("All Fields Invalid")
    class AllFieldsInvalid {

        @Test
        @DisplayName("Should return violations for all invalid fields")
        void shouldReturnViolationsForAllFields() {
            UserRegistrationRequest request = new UserRegistrationRequest("", "", "");
            Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
            assertTrue(violations.size() >= 3);
        }
    }
}
