package com.inventory.common.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.common.exception.ErrorResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Filter that validates tenant context for protected endpoints.
 *
 * For regular endpoints: Requires X-Tenant-Id header and validates it against JWT token tenant
 * For admin endpoints: GLOBAL_ADMIN can optionally use X-Tenant-Id header to query specific tenant data
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

        // For admin paths, GLOBAL_ADMIN can optionally use X-Tenant-Id header
        if (isAdminPath(path)) {
            handleAdminPath(request, response, filterChain);
            return;
        }

        // For regular paths, X-Tenant-Id header is REQUIRED
        String headerTenantId = request.getHeader(TENANT_HEADER);
        if (!StringUtils.hasText(headerTenantId)) {
            log.warn("Missing X-Tenant-Id header for path: {}", path);
            sendErrorResponse(response, HttpStatus.BAD_REQUEST,
                "Missing required header: X-Tenant-Id");
            return;
        }

        // Get tenant from JWT token (set by JwtAuthenticationFilter)
        String tokenTenantId = TenantContext.getTenantId();

        // Validate that header tenant matches token tenant (prevent tenant spoofing)
        if (StringUtils.hasText(tokenTenantId) && !tokenTenantId.equals(headerTenantId)) {
            log.warn("Tenant mismatch - Header: {}, Token: {}", headerTenantId, tokenTenantId);
            sendErrorResponse(response, HttpStatus.FORBIDDEN,
                "Access denied: X-Tenant-Id does not match authenticated user's tenant");
            return;
        }

        // Set tenant context from header (this takes precedence)
        TenantContext.setTenantId(headerTenantId);
        log.debug("Processing request for tenant: {}, path: {}", headerTenantId, path);

        filterChain.doFilter(request, response);
    }

    private void handleAdminPath(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verify user is GLOBAL_ADMIN for admin paths
        if (authentication == null || !authentication.isAuthenticated()) {
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Authentication required");
            return;
        }

        boolean isGlobalAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(Role.GLOBAL_ADMIN::matches);

        if (!isGlobalAdmin) {
            log.warn("Non-admin user attempted to access admin path");
            sendErrorResponse(response, HttpStatus.FORBIDDEN,
                "Access denied: GLOBAL_ADMIN role required");
            return;
        }

        // GLOBAL_ADMIN can optionally use X-Tenant-Id header for per-tenant queries
        String headerTenantId = request.getHeader(TENANT_HEADER);
        if (StringUtils.hasText(headerTenantId)) {
            // Override tenant context with header value for this request
            TenantContext.setTenantId(headerTenantId);
            log.debug("Admin override: using tenant {} from header", headerTenantId);
        }

        filterChain.doFilter(request, response);
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
