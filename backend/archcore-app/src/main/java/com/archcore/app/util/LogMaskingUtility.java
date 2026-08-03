package com.archcore.app.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LogMaskingUtility {

    private static final String MASK = "****";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "([\\w.+-]+)@([\\w.-]+\\.[a-zA-Z]{2,})", Pattern.CASE_INSENSITIVE);
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "(password|passwd|pwd|sifre|secret|token|apikey|api_key|authorization)([\\s]*[=:]\\s*[\"']?)(.+?)([\"']?)$",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile(
        "\\b(\\d{4})[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?(\\d{4})\\b");
    private static final Pattern SSN_PATTERN = Pattern.compile(
        "\\b(\\d{3})[\\s-]?\\d{2}[\\s-]?(\\d{4})\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "(\\+?\\d{1,3}[\\s-]?)?(\\(\\d{2,4}\\)|\\d{2,4})[\\s.-]?\\d{3,4}[\\s.-]?\\d{3,4}");

    private LogMaskingUtility() {
    }

    public static String mask(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        String result = maskPasswords(input);
        result = maskCreditCards(result);
        result = maskSSN(result);
        result = maskEmails(result);
        return result;
    }

    private static String maskPasswords(String input) {
        Matcher matcher = PASSWORD_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String separator = matcher.group(2);
            String closingQuote = matcher.group(4);
            matcher.appendReplacement(sb, key + separator + MASK + (closingQuote != null ? closingQuote : ""));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String maskCreditCards(String input) {
        Matcher matcher = CREDIT_CARD_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1) + " **** **** " + matcher.group(2));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String maskSSN(String input) {
        Matcher matcher = SSN_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "***-**-" + matcher.group(2));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String maskEmails(String input) {
        Matcher matcher = EMAIL_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String username = matcher.group(1);
            String domain = matcher.group(2);
            String maskedUsername = username.length() <= 2
                ? MASK
                : username.substring(0, 2) + MASK;
            matcher.appendReplacement(sb, maskedUsername + "@" + domain);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
