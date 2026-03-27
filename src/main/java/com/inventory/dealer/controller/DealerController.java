package com.inventory.dealer.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inventory.common.web.PageResponse;
import com.inventory.dealer.dto.CreateDealerRequest;
import com.inventory.dealer.dto.DealerResponse;
import com.inventory.dealer.dto.UpdateDealerRequest;
import com.inventory.dealer.service.DealerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dealers")
@RequiredArgsConstructor
@Tag(name = "Dealers", description = "Dealer management endpoints")
public class DealerController {

    private final DealerService dealerService;

    @PostMapping
    @Operation(summary = "Create a new dealer",
            description = "Creates a new dealer for the current tenant")
    public ResponseEntity<DealerResponse> createDealer(@Valid @RequestBody CreateDealerRequest request) {
        DealerResponse response = dealerService.createDealer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get dealer by ID",
            description = "Retrieves a dealer by ID within the current tenant")
    public ResponseEntity<DealerResponse> getDealerById(
            @Parameter(description = "Dealer ID") @PathVariable UUID id) {
        DealerResponse response = dealerService.getDealerById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all dealers",
            description = "Retrieves all dealers for the current tenant with pagination and sorting")
    public ResponseEntity<PageResponse<DealerResponse>> getAllDealers(
            @PageableDefault(size = 10, sort = "auditable.createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<DealerResponse> page = dealerService.getAllDealers(pageable);
        return ResponseEntity.ok(PageResponse.of(page));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a dealer",
            description = "Partially updates a dealer within the current tenant")
    public ResponseEntity<DealerResponse> updateDealer(
            @Parameter(description = "Dealer ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateDealerRequest request) {
        DealerResponse response = dealerService.updateDealer(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a dealer",
            description = "Deletes a dealer within the current tenant")
    public ResponseEntity<Void> deleteDealer(
            @Parameter(description = "Dealer ID") @PathVariable UUID id) {
        dealerService.deleteDealer(id);
        return ResponseEntity.noContent().build();
    }
}
