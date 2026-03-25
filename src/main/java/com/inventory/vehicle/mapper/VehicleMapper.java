package com.inventory.vehicle.mapper;

import com.inventory.dealer.entity.Dealer;
import com.inventory.vehicle.dto.CreateVehicleRequest;
import com.inventory.vehicle.dto.VehicleResponse;
import com.inventory.vehicle.entity.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    public Vehicle toEntity(CreateVehicleRequest request, String tenantId, Dealer dealer) {
        return Vehicle.builder()
                .tenantId(tenantId)
                .dealer(dealer)
                .model(request.getModel())
                .price(request.getPrice())
                .status(request.getStatus())
                .build();
    }

    public VehicleResponse toResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .tenantId(vehicle.getTenantId())
                .dealerId(vehicle.getDealer().getId())
                .dealerName(vehicle.getDealer().getName())
                .dealerSubscription(vehicle.getDealer().getSubscriptionType())
                .model(vehicle.getModel())
                .price(vehicle.getPrice())
                .status(vehicle.getStatus())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}
