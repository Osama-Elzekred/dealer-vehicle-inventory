package com.inventory.vehicle.dto;

import com.inventory.vehicle.entity.VehicleStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVehicleRequest {

    @NotNull(message = "Dealer ID is required")
    private UUID dealerId;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Status is required")
    private VehicleStatus status;
}
