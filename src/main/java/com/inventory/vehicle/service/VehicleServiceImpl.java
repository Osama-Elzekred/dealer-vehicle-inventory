package com.inventory.vehicle.service;

import com.inventory.common.exception.ResourceNotFoundException;
import com.inventory.common.exception.TenantAccessDeniedException;
import com.inventory.common.security.TenantContext;
import com.inventory.dealer.entity.Dealer;
import com.inventory.dealer.repository.DealerRepository;
import com.inventory.vehicle.dto.CreateVehicleRequest;
import com.inventory.vehicle.dto.UpdateVehicleRequest;
import com.inventory.vehicle.dto.VehicleFilterRequest;
import com.inventory.vehicle.dto.VehicleResponse;
import com.inventory.vehicle.entity.Vehicle;
import com.inventory.vehicle.mapper.VehicleMapper;
import com.inventory.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DealerRepository dealerRepository;
    private final VehicleMapper vehicleMapper;

    @Override
    public VehicleResponse createVehicle(CreateVehicleRequest request) {
        String tenantId = getCurrentTenantId();
        log.debug("Creating vehicle for tenant: {}", tenantId);

        // Verify dealer exists and belongs to same tenant
        Dealer dealer = dealerRepository.findByIdAndTenantId(request.getDealerId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer", "id", request.getDealerId()));

        Vehicle vehicle = vehicleMapper.toEntity(request, tenantId, dealer);
        vehicle = vehicleRepository.save(vehicle);

        log.info("Created vehicle: {} for tenant: {}", vehicle.getId(), tenantId);
        return vehicleMapper.toResponse(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(UUID id) {
        String tenantId = getCurrentTenantId();
        log.debug("Fetching vehicle: {} for tenant: {}", id, tenantId);

        Vehicle vehicle = vehicleRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));

        return vehicleMapper.toResponse(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> getAllVehicles(VehicleFilterRequest filter, Pageable pageable) {
        String tenantId = getCurrentTenantId();
        log.debug("Fetching vehicles for tenant: {} with filters: {}", tenantId, filter);

        Page<Vehicle> vehicles = vehicleRepository.findByFilters(
                tenantId,
                filter.getModel(),
                filter.getStatus(),
                filter.getPriceMin(),
                filter.getPriceMax(),
                filter.getSubscription(),
                pageable
        );

        return vehicles.map(vehicleMapper::toResponse);
    }

    @Override
    public VehicleResponse updateVehicle(UUID id, UpdateVehicleRequest request) {
        String tenantId = getCurrentTenantId();
        log.debug("Updating vehicle: {} for tenant: {}", id, tenantId);

        Vehicle vehicle = vehicleRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));

        if (request.getDealerId() != null) {
            Dealer dealer = dealerRepository.findByIdAndTenantId(request.getDealerId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Dealer", "id", request.getDealerId()));
            vehicle.setDealer(dealer);
        }
        if (request.getModel() != null) {
            vehicle.setModel(request.getModel());
        }
        if (request.getPrice() != null) {
            vehicle.setPrice(request.getPrice());
        }
        if (request.getStatus() != null) {
            vehicle.setStatus(request.getStatus());
        }

        vehicle = vehicleRepository.save(vehicle);
        log.info("Updated vehicle: {} for tenant: {}", id, tenantId);

        return vehicleMapper.toResponse(vehicle);
    }

    @Override
    public void deleteVehicle(UUID id) {
        String tenantId = getCurrentTenantId();
        log.debug("Deleting vehicle: {} for tenant: {}", id, tenantId);

        if (!vehicleRepository.existsByIdAndTenantId(id, tenantId)) {
            throw new ResourceNotFoundException("Vehicle", "id", id);
        }

        vehicleRepository.deleteById(id);
        log.info("Deleted vehicle: {} for tenant: {}", id, tenantId);
    }

    private String getCurrentTenantId() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new TenantAccessDeniedException("No tenant context available");
        }
        return tenantId;
    }
}
