package com.inventory.vehicle.dto;

import com.inventory.vehicle.entity.VehicleStatus;
import jakarta.validation.constraints.DecimalMin;
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
public class UpdateVehicleRequest {

    private UUID dealerId;

    private String model;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    private VehicleStatus status;
}
