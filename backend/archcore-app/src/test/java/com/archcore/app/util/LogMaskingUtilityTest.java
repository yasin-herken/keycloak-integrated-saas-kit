package com.archcore.app.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogMaskingUtilityTest {

    @Nested
    @DisplayName("Password Masking")
    class PasswordMasking {

        @Test
        @DisplayName("Should mask password with equals sign")
        void shouldMaskPasswordWithEquals() {
            String input = "password=secret123";
            String result = LogMaskingUtility.mask(input);
            assertEquals("password=****", result);
        }

        @Test
        @DisplayName("Should mask password with colon separator")
        void shouldMaskPasswordWithColon() {
            String input = "password: secret123";
            String result = LogMaskingUtility.mask(input);
            assertEquals("password: ****", result);
        }

        @Test
        @DisplayName("Should mask pwd keyword")
        void shouldMaskPwdKeyword() {
            String input = "pwd=mySecretPass";
            String result = LogMaskingUtility.mask(input);
            assertEquals("pwd=****", result);
        }

        @Test
        @DisplayName("Should mask secret keyword")
        void shouldMaskSecretKeyword() {
            String input = "secret=abc123";
            String result = LogMaskingUtility.mask(input);
            assertEquals("secret=****", result);
        }

        @Test
        @DisplayName("Should mask token keyword")
        void shouldMaskTokenKeyword() {
            String input = "token=eyJhbGciOiJIUzI1NiJ9";
            String result = LogMaskingUtility.mask(input);
            assertEquals("token=****", result);
        }

        @Test
        @DisplayName("Should mask authorization header")
        void shouldMaskAuthorizationHeader() {
            String input = "authorization=Bearer eyJhbGciOiJSUzI1NiJ9";
            String result = LogMaskingUtility.mask(input);
            assertEquals("authorization=****", result);
        }

        @Test
        @DisplayName("Should mask password in quoted value")
        void shouldMaskPasswordInQuotedValue() {
            String input = "password=\"mySecret\"";
            String result = LogMaskingUtility.mask(input);
            assertEquals("password=\"****\"", result);
        }
    }

    @Nested
    @DisplayName("Credit Card Masking")
    class CreditCardMasking {

        @Test
        @DisplayName("Should mask credit card number")
        void shouldMaskCreditCardNumber() {
            String input = "Card: 4111 1111 1111 1111";
            String result = LogMaskingUtility.mask(input);
            assertEquals("Card: 4111 **** **** 1111", result);
        }

        @Test
        @DisplayName("Should mask credit card with dashes")
        void shouldMaskCreditCardWithDashes() {
            String input = "Card: 4111-1111-1111-1111";
            String result = LogMaskingUtility.mask(input);
            assertEquals("Card: 4111 **** **** 1111", result);
        }

        @Test
        @DisplayName("Should mask credit card without separators")
        void shouldMaskCreditCardWithoutSeparators() {
            String input = "Card: 4111111111111111";
            String result = LogMaskingUtility.mask(input);
            assertEquals("Card: 4111 **** **** 1111", result);
        }
    }

    @Nested
    @DisplayName("SSN Masking")
    class SSNMasking {

        @Test
        @DisplayName("Should mask SSN with dashes")
        void shouldMaskSSNWithDashes() {
            String input = "SSN: 123-45-6789";
            String result = LogMaskingUtility.mask(input);
            assertEquals("SSN: ***-**-6789", result);
        }

        @Test
        @DisplayName("Should mask SSN without dashes")
        void shouldMaskSSNWithoutDashes() {
            String input = "SSN: 123456789";
            String result = LogMaskingUtility.mask(input);
            assertEquals("SSN: ***-**-6789", result);
        }
    }

    @Nested
    @DisplayName("Email Masking")
    class EmailMasking {

        @Test
        @DisplayName("Should partially mask email username")
        void shouldPartiallyMaskEmailUsername() {
            String input = "Email: john.doe@example.com";
            String result = LogMaskingUtility.mask(input);
            assertEquals("Email: jo****@example.com", result);
        }

        @Test
        @DisplayName("Should mask short email username completely")
        void shouldMaskShortEmailUsernameCompletely() {
            String input = "ab@test.com";
            String result = LogMaskingUtility.mask(input);
            assertEquals("****@test.com", result);
        }
    }

    @Nested
    @DisplayName("Null and Empty Input")
    class NullAndEmpty {

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertNull(LogMaskingUtility.mask(null));
        }

        @Test
        @DisplayName("Should return blank for blank input")
        void shouldReturnBlankForBlankInput() {
            assertEquals("   ", LogMaskingUtility.mask("   "));
        }

        @Test
        @DisplayName("Should return empty string for empty input")
        void shouldReturnEmptyStringForEmptyInput() {
            assertEquals("", LogMaskingUtility.mask(""));
        }
    }

    @Nested
    @DisplayName("Combined Sensitive Data")
    class CombinedSensitiveData {

        @Test
        @DisplayName("Should mask multiple sensitive fields in one string")
        void shouldMaskMultipleFields() {
            String input = "user=admin password=secret123 email=test@example.com";
            String result = LogMaskingUtility.mask(input);
            assertTrue(result.contains("password=****"));
            assertTrue(result.contains("****@example.com"));
        }

        @Test
        @DisplayName("Should not mask non-sensitive data")
        void shouldNotMaskNonSensitiveData() {
            String input = "User logged in from 192.168.1.1";
            String result = LogMaskingUtility.mask(input);
            assertEquals("User logged in from 192.168.1.1", result);
        }
    }
}
