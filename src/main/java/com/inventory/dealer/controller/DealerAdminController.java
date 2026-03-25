package com.inventory.dealer.controller;

import com.inventory.dealer.service.DealerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/dealers")
@RequiredArgsConstructor
@Tag(name = "Admin - Dealers", description = "Admin-only dealer management endpoints (requires GLOBAL_ADMIN role)")
public class DealerAdminController {

    private final DealerService dealerService;

    @GetMapping("/countBySubscription")
    @Operation(summary = "Count dealers by subscription type",
            description = """
                    Returns the count of dealers grouped by subscription type.

                    **Scope behavior:**
                    - Default (no scope param): Returns system-wide counts across all tenants
                    - With `scope=tenant`: Returns counts scoped to the X-Tenant-Id provided in the header

                    Requires GLOBAL_ADMIN role.
                    """)
    public ResponseEntity<Map<String, Long>> countBySubscription(
            @Parameter(description = "Scope: 'tenant' for per-tenant counts, omit for system-wide")
            @RequestParam(required = false) String scope) {
        boolean perTenant = "tenant".equalsIgnoreCase(scope);
        Map<String, Long> counts = dealerService.countBySubscription(perTenant);
        return ResponseEntity.ok(counts);
    }
}
