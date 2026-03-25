package com.inventory.vehicle.dto;

import com.inventory.dealer.entity.SubscriptionType;
import com.inventory.vehicle.entity.VehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleFilterRequest {

    private String model;
    private VehicleStatus status;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private SubscriptionType subscription;
}
