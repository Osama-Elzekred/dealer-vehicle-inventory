package com.inventory.dealer.dto;

import com.inventory.dealer.entity.SubscriptionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealerResponse {

    private UUID id;
    private String tenantId;
    private String name;
    private String email;
    private SubscriptionType subscriptionType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
