package com.inventory.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.common.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filter that validates X-Tenant-Id header for protected endpoints.
 *
 * Requirements:
 * - Missing X-Tenant-Id → 400 Bad Request
 * - Cross-tenant access blocked → 403 Forbidden
 * - Admin endpoints require GLOBAL_ADMIN role
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-Id";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/auth/**",
            "/h2-console/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/error",
            "/webjars/**"
    );

    private static final List<String> ADMIN_PATHS = Arrays.asList(
            "/admin/**"
    );

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // Skip tenant validation for excluded paths (auth, swagger, h2-console, etc.)
        if (isExcludedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // For admin paths, tenant header is optional (for per-tenant queries)
        if (isAdminPath(path)) {
            handleAdminPath(request, response, filterChain);
            return;
        }

        // For regular paths, X-Tenant-Id header is REQUIRED
        String tenantId = request.getHeader(TENANT_HEADER);
        if (!StringUtils.hasText(tenantId)) {
            log.warn("Missing X-Tenant-Id header for path: {}", path);
            sendErrorResponse(response, HttpStatus.BAD_REQUEST, "Missing required header: X-Tenant-Id");
            return;
        }

        // Validate tenant matches authenticated user's tenant (if authenticated)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {

            // Allow GLOBAL_ADMIN to access any tenant
            boolean isGlobalAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals(RoleConstants.ROLE_GLOBAL_ADMIN));

            if (!isGlobalAdmin) {
                // For regular users, the tenant from header must match their token's tenant
                String tokenTenant = TenantContext.getTokenTenantId();
                if (tokenTenant != null && !tokenTenant.equals(tenantId)) {
                    log.warn("Cross-tenant access blocked: token={}, header={}", tokenTenant, tenantId);
                    sendErrorResponse(response, HttpStatus.FORBIDDEN,
                            "Access denied: Cannot access resources of different tenant");
                    return;
                }
            }
        }

        // Set the tenant context from header
        TenantContext.setTenantId(tenantId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private void handleAdminPath(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verify user is authenticated
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Authentication required");
            return;
        }

        // Verify user is GLOBAL_ADMIN for admin paths
        boolean isGlobalAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals(RoleConstants.ROLE_GLOBAL_ADMIN));

        if (!isGlobalAdmin) {
            log.warn("Non-admin user attempted to access admin path");
            sendErrorResponse(response, HttpStatus.FORBIDDEN, "Access denied: GLOBAL_ADMIN role required");
            return;
        }

        // For admin paths, X-Tenant-Id is optional (for per-tenant queries)
        String tenantId = request.getHeader(TENANT_HEADER);
        if (StringUtils.hasText(tenantId)) {
            TenantContext.setTenantId(tenantId);
            log.debug("Admin using tenant context: {}", tenantId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    private boolean isAdminPath(String path) {
        return ADMIN_PATHS.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse errorResponse = new ErrorResponse(status.value(), message);
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
