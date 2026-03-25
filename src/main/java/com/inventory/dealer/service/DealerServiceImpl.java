package com.inventory.dealer.service;

import com.inventory.common.exception.ResourceNotFoundException;
import com.inventory.common.exception.TenantAccessDeniedException;
import com.inventory.common.security.TenantContext;
import com.inventory.dealer.dto.CreateDealerRequest;
import com.inventory.dealer.dto.DealerResponse;
import com.inventory.dealer.dto.UpdateDealerRequest;
import com.inventory.dealer.entity.Dealer;
import com.inventory.dealer.entity.SubscriptionType;
import com.inventory.dealer.mapper.DealerMapper;
import com.inventory.dealer.repository.DealerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DealerServiceImpl implements DealerService {

    private final DealerRepository dealerRepository;
    private final DealerMapper dealerMapper;

    @Override
    public DealerResponse createDealer(CreateDealerRequest request) {
        String tenantId = getCurrentTenantId();
        log.debug("Creating dealer for tenant: {}", tenantId);

        Dealer dealer = dealerMapper.toEntity(request, tenantId);
        dealer = dealerRepository.save(dealer);

        log.info("Created dealer: {} for tenant: {}", dealer.getId(), tenantId);
        return dealerMapper.toResponse(dealer);
    }

    @Override
    @Transactional(readOnly = true)
    public DealerResponse getDealerById(UUID id) {
        String tenantId = getCurrentTenantId();
        log.debug("Fetching dealer: {} for tenant: {}", id, tenantId);

        Dealer dealer = dealerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer", "id", id));

        return dealerMapper.toResponse(dealer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DealerResponse> getAllDealers(Pageable pageable) {
        String tenantId = getCurrentTenantId();
        log.debug("Fetching all dealers for tenant: {}", tenantId);

        return dealerRepository.findByTenantId(tenantId, pageable)
                .map(dealerMapper::toResponse);
    }

    @Override
    public DealerResponse updateDealer(UUID id, UpdateDealerRequest request) {
        String tenantId = getCurrentTenantId();
        log.debug("Updating dealer: {} for tenant: {}", id, tenantId);

        Dealer dealer = dealerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer", "id", id));

        if (request.getName() != null) {
            dealer.setName(request.getName());
        }
        if (request.getEmail() != null) {
            dealer.setEmail(request.getEmail());
        }
        if (request.getSubscriptionType() != null) {
            dealer.setSubscriptionType(request.getSubscriptionType());
        }

        dealer = dealerRepository.save(dealer);
        log.info("Updated dealer: {} for tenant: {}", id, tenantId);

        return dealerMapper.toResponse(dealer);
    }

    @Override
    public void deleteDealer(UUID id) {
        String tenantId = getCurrentTenantId();
        log.debug("Deleting dealer: {} for tenant: {}", id, tenantId);

        if (!dealerRepository.existsByIdAndTenantId(id, tenantId)) {
            throw new ResourceNotFoundException("Dealer", "id", id);
        }

        dealerRepository.deleteById(id);
        log.info("Deleted dealer: {} for tenant: {}", id, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> countBySubscription(boolean perTenant) {
        Map<String, Long> counts = new LinkedHashMap<>();

        if (perTenant) {
            String tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                throw new IllegalArgumentException("Tenant ID required for per-tenant count");
            }
            counts.put("BASIC", dealerRepository.countBySubscriptionTypeAndTenantId(SubscriptionType.BASIC, tenantId));
            counts.put("PREMIUM", dealerRepository.countBySubscriptionTypeAndTenantId(SubscriptionType.PREMIUM, tenantId));
            log.debug("Counted dealers by subscription for tenant: {}", tenantId);
        } else {
            counts.put("BASIC", dealerRepository.countBySubscriptionType(SubscriptionType.BASIC));
            counts.put("PREMIUM", dealerRepository.countBySubscriptionType(SubscriptionType.PREMIUM));
            log.debug("Counted dealers by subscription system-wide");
        }

        return counts;
    }

    private String getCurrentTenantId() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new TenantAccessDeniedException("No tenant context available");
        }
        return tenantId;
    }
}
