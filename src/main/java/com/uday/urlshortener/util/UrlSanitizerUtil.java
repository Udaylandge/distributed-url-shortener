package com.uday.urlshortener.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.regex.Pattern;

public class UrlSanitizerUtil {

    private static final Pattern DISALLOWED_SCHEMES = Pattern.compile("^(javascript|data|vbscript|file):", Pattern.CASE_INSENSITIVE);

    /**
     * Sanitizes and normalizes a user-supplied URL.
     * 1. Trims whitespace.
     * 2. Checks for dangerous protocols (javascript:, data:, file:).
     * 3. Prepends https:// if no protocol is specified.
     * 4. Supports http://, https://, and ftp://.
     */
    public static String sanitizeAndNormalizeUrl(String inputUrl) {
        if (inputUrl == null || inputUrl.isBlank()) {
            throw new IllegalArgumentException("URL cannot be empty or null.");
        }

        String trimmed = inputUrl.trim();

        if (DISALLOWED_SCHEMES.matcher(trimmed).find()) {
            throw new IllegalArgumentException("Invalid URL scheme provided.");
        }

        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("http://") && !lower.startsWith("https://") && !lower.startsWith("ftp://")) {
            trimmed = "https://" + trimmed;
        }

        try {
            URI uri = new URI(trimmed);
            if (uri.getHost() == null && !trimmed.startsWith("ftp://")) {
                throw new IllegalArgumentException("Invalid domain structure in URL.");
            }
            return trimmed;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL syntax: " + e.getMessage());
        }
    }

    /**
     * Validates whether a given string is a syntactically valid target URL.
     */
    public static boolean isValidUrl(String inputUrl) {
        if (inputUrl == null || inputUrl.isBlank()) {
            return false;
        }
        try {
            sanitizeAndNormalizeUrl(inputUrl);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
