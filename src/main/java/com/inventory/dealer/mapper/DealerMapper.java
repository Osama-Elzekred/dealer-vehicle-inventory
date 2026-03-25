package com.inventory.dealer.mapper;

import com.inventory.dealer.dto.CreateDealerRequest;
import com.inventory.dealer.dto.DealerResponse;
import com.inventory.dealer.entity.Dealer;
import org.springframework.stereotype.Component;

@Component
public class DealerMapper {

    public Dealer toEntity(CreateDealerRequest request, String tenantId) {
        return Dealer.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .email(request.getEmail())
                .subscriptionType(request.getSubscriptionType())
                .build();
    }

    public DealerResponse toResponse(Dealer dealer) {
        return DealerResponse.builder()
                .id(dealer.getId())
                .tenantId(dealer.getTenantId())
                .name(dealer.getName())
                .email(dealer.getEmail())
                .subscriptionType(dealer.getSubscriptionType())
                .createdAt(dealer.getCreatedAt())
                .updatedAt(dealer.getUpdatedAt())
                .build();
    }
}
