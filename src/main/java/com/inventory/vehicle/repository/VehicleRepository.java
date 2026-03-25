package com.inventory.vehicle.repository;

import com.inventory.dealer.entity.SubscriptionType;
import com.inventory.vehicle.entity.Vehicle;
import com.inventory.vehicle.entity.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID>, JpaSpecificationExecutor<Vehicle> {

    Page<Vehicle> findByTenantId(String tenantId, Pageable pageable);

    Optional<Vehicle> findByIdAndTenantId(UUID id, String tenantId);

    boolean existsByIdAndTenantId(UUID id, String tenantId);

    @Query("SELECT v FROM Vehicle v JOIN v.dealer d WHERE v.tenantId = :tenantId " +
            "AND (:model IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%'))) " +
            "AND (:status IS NULL OR v.status = :status) " +
            "AND (:priceMin IS NULL OR v.price >= :priceMin) " +
            "AND (:priceMax IS NULL OR v.price <= :priceMax) " +
            "AND (:subscription IS NULL OR d.subscriptionType = :subscription)")
    Page<Vehicle> findByFilters(
            @Param("tenantId") String tenantId,
            @Param("model") String model,
            @Param("status") VehicleStatus status,
            @Param("priceMin") BigDecimal priceMin,
            @Param("priceMax") BigDecimal priceMax,
            @Param("subscription") SubscriptionType subscription,
            Pageable pageable);

    void deleteByIdAndTenantId(UUID id, String tenantId);
}
