package com.electrahub.subscription.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AccountContextResolver {

    private static final Pattern UID_PATTERN = Pattern.compile("\"uid\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ROLES_PATTERN = Pattern.compile("\"roles\"\\s*:\\s*\\[([^\\]]*)\\]");
    private static final Pattern ROLE_PATTERN = Pattern.compile("\"([^\"]+)\"");

    public String resolveAccountId(HttpServletRequest request) {
        String explicitAccountId = trimToNull(request.getHeader("X-Account-Id"));
        if (explicitAccountId != null) {
            return explicitAccountId;
        }

        String authorization = trimToNull(request.getHeader("Authorization"));
        if (authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String uid = extractUid(authorization.substring(7).trim());
            if (uid != null) {
                return uid;
            }
        }
        return "anonymous";
    }

    public boolean hasRole(HttpServletRequest request, String expectedRole) {
        String authorization = trimToNull(request.getHeader("Authorization"));
        if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return false;
        }
        return extractRoles(authorization.substring(7).trim()).stream()
                .anyMatch(role -> role.equalsIgnoreCase(expectedRole));
    }

    private String extractUid(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Matcher matcher = UID_PATTERN.matcher(payload);
            return matcher.find() ? trimToNull(matcher.group(1)) : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<String> extractRoles(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length != 3) {
                return List.of();
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Matcher rolesMatcher = ROLES_PATTERN.matcher(payload);
            if (!rolesMatcher.find()) {
                return List.of();
            }
            Matcher roleMatcher = ROLE_PATTERN.matcher(rolesMatcher.group(1));
            java.util.ArrayList<String> roles = new java.util.ArrayList<>();
            while (roleMatcher.find()) {
                String role = trimToNull(roleMatcher.group(1));
                if (role != null) {
                    roles.add(role);
                }
            }
            return roles;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
