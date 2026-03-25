package com.inventory.vehicle.dto;

import com.inventory.dealer.entity.SubscriptionType;
import com.inventory.vehicle.entity.VehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponse {

    private UUID id;
    private String tenantId;
    private UUID dealerId;
    private String dealerName;
    private SubscriptionType dealerSubscription;
    private String model;
    private BigDecimal price;
    private VehicleStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
