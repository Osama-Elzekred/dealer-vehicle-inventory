package com.inventory.auth.controller;

import com.inventory.auth.dto.LoginRequest;
import com.inventory.auth.dto.TokenResponse;
import com.inventory.common.security.JwtTokenProvider;
import com.inventory.common.security.RoleConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints for JWT token generation")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    @Operation(summary = "Generate JWT token",
            description = "Generate a JWT token for testing. Use role 'TENANT_USER' or 'GLOBAL_ADMIN'")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        String role = request.getRole().toUpperCase();
        
        if (!role.equals(RoleConstants.TENANT_USER) && !role.equals(RoleConstants.GLOBAL_ADMIN)) {
            throw new IllegalArgumentException("Invalid role. Must be TENANT_USER or GLOBAL_ADMIN");
        }

        String token = jwtTokenProvider.generateToken(
                request.getUsername(),
                request.getTenantId(),
                List.of(role)
        );

        TokenResponse response = TokenResponse.builder()
                .token(token)
                .username(request.getUsername())
                .tenantId(request.getTenantId())
                .role(role)
                .build();

        return ResponseEntity.ok(response);
    }
}
