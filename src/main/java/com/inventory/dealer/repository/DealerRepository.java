package com.inventory.dealer.repository;

import com.inventory.dealer.entity.Dealer;
import com.inventory.dealer.entity.SubscriptionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DealerRepository extends JpaRepository<Dealer, UUID> {

    Page<Dealer> findByTenantId(String tenantId, Pageable pageable);

    Optional<Dealer> findByIdAndTenantId(UUID id, String tenantId);

    boolean existsByIdAndTenantId(UUID id, String tenantId);

    long countBySubscriptionType(SubscriptionType type);

    long countBySubscriptionTypeAndTenantId(@Param("type") SubscriptionType type, @Param("tenantId") String tenantId);
}
