package com.inventory.vehicle.controller;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inventory.common.web.PageResponse;
import com.inventory.dealer.entity.SubscriptionType;
import com.inventory.vehicle.dto.CreateVehicleRequest;
import com.inventory.vehicle.dto.UpdateVehicleRequest;
import com.inventory.vehicle.dto.VehicleFilterRequest;
import com.inventory.vehicle.dto.VehicleResponse;
import com.inventory.vehicle.entity.VehicleStatus;
import com.inventory.vehicle.service.VehicleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Vehicle management endpoints")
public class VehicleController {

    private final VehicleService vehicleService;
    

    @PostMapping
    @Operation(summary = "Create a new vehicle",
            description = "Creates a new vehicle for the current tenant")
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody CreateVehicleRequest request) {
        VehicleResponse response = vehicleService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID",
            description = "Retrieves a vehicle by ID within the current tenant")
    public ResponseEntity<VehicleResponse> getVehicleById(
            @Parameter(description = "Vehicle ID") @PathVariable UUID id) {
        VehicleResponse response = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all vehicles",
            description = """
                    Retrieves all vehicles for the current tenant with optional filters, pagination and sorting.

                    **Available filters:**
                    - model: Filter by model name (case-insensitive, partial match)
                    - status: Filter by status (AVAILABLE, SOLD)
                    - priceMin: Minimum price
                    - priceMax: Maximum price
                    - subscription: Filter by dealer's subscription type (BASIC, PREMIUM)

                    **Example:** GET /vehicles?subscription=PREMIUM returns only vehicles from PREMIUM dealers
                    """)
    public ResponseEntity<PageResponse<VehicleResponse>> getAllVehicles(
            @Parameter(description = "Filter by model name (partial match)")
            @RequestParam(required = false) String model,

            @Parameter(description = "Filter by vehicle status")
            @RequestParam(required = false) VehicleStatus status,

            @Parameter(description = "Minimum price filter")
            @RequestParam(required = false) BigDecimal priceMin,

            @Parameter(description = "Maximum price filter")
            @RequestParam(required = false) BigDecimal priceMax,

            @Parameter(description = "Filter by dealer's subscription type")
            @RequestParam(required = false) SubscriptionType subscription,

            @PageableDefault(size = 10, sort = "auditable.createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        VehicleFilterRequest filter = VehicleFilterRequest.builder()
                .model(model)
                .status(status)
                .priceMin(priceMin)
                .priceMax(priceMax)
                .subscription(subscription)
                .build();

        Page<VehicleResponse> page = vehicleService.getAllVehicles(filter, pageable);
        return ResponseEntity.ok(PageResponse.of(page));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a vehicle",
            description = "Partially updates a vehicle within the current tenant")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateVehicleRequest request) {
        VehicleResponse response = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a vehicle",
            description = "Deletes a vehicle within the current tenant")
    public ResponseEntity<Void> deleteVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable UUID id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
