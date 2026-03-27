package com.inventory.common.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    TENANT_USER("ROLE_TENANT_USER"),
    GLOBAL_ADMIN("ROLE_GLOBAL_ADMIN");

    private final String authority;

    /**
     * Get the role name without ROLE_ prefix (for JWT token).
     */
    public String getName() {
        return this.name();
    }

    /**
     * Get the Spring Security authority (with ROLE_ prefix).
     */
    public String getAuthority() {
        return this.authority;
    }

    /**
     * Check if a given authority string matches this role.
     */
    public boolean matches(String authority) {
        return this.authority.equals(authority) || this.name().equals(authority);
    }

    /**
     * Parse role from string (accepts both "TENANT_USER" and "ROLE_TENANT_USER").
     */
    public static Role fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        String normalized = value.toUpperCase().replace("ROLE_", "");

        for (Role role : values()) {
            if (role.name().equals(normalized)) {
                return role;
            }
        }

        throw new IllegalArgumentException("Invalid role: " + value + ". Must be TENANT_USER or GLOBAL_ADMIN");
    }

    /**
     * Check if a string represents a valid role.
     */
    public static boolean isValid(String value) {
        try {
            fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
