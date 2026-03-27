package com.inventory.dealer.mapper;

import com.inventory.dealer.dto.CreateDealerRequest;
import com.inventory.dealer.dto.DealerResponse;
import com.inventory.dealer.entity.Dealer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DealerMapper {

    public static Dealer toEntity(CreateDealerRequest request, String tenantId) {
        return Dealer.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .email(request.getEmail())
                .subscriptionType(request.getSubscriptionType())
                .build();
    }

    public static DealerResponse toResponse(Dealer dealer) {
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
