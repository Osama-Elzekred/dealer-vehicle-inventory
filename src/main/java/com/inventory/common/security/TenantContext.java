package com.inventory.common.security;

/**
 * Thread-local context for multi-tenant operations.
 *
 * - tenantId: The active tenant ID from X-Tenant-Id header (used for queries)
 * - tokenTenantId: The tenant ID from JWT token (used for validation)
 */
public final class TenantContext {

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    private static final ThreadLocal<String> tokenTenant = new ThreadLocal<>();

    private TenantContext() {
    }

    /**
     * Set the active tenant ID (from X-Tenant-Id header).
     */
    public static void setTenantId(String tenantId) {
        currentTenant.set(tenantId);
    }

    /**
     * Get the active tenant ID.
     */
    public static String getTenantId() {
        return currentTenant.get();
    }

    /**
     * Set the tenant ID from JWT token (for validation purposes).
     */
    public static void setTokenTenantId(String tenantId) {
        tokenTenant.set(tenantId);
    }

    /**
     * Get the tenant ID from JWT token.
     */
    public static String getTokenTenantId() {
        return tokenTenant.get();
    }

    /**
     * Clear all tenant context.
     */
    public static void clear() {
        currentTenant.remove();
        tokenTenant.remove();
    }
}
