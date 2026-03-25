package com.inventory.vehicle.service;

import com.inventory.vehicle.dto.CreateVehicleRequest;
import com.inventory.vehicle.dto.UpdateVehicleRequest;
import com.inventory.vehicle.dto.VehicleFilterRequest;
import com.inventory.vehicle.dto.VehicleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VehicleService {

    VehicleResponse createVehicle(CreateVehicleRequest request);

    VehicleResponse getVehicleById(UUID id);

    Page<VehicleResponse> getAllVehicles(VehicleFilterRequest filter, Pageable pageable);

    VehicleResponse updateVehicle(UUID id, UpdateVehicleRequest request);

    void deleteVehicle(UUID id);
}
