package com.inventory.dealer.dto;

import com.inventory.dealer.entity.SubscriptionType;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDealerRequest {

    private String name;

    @Email(message = "Invalid email format")
    private String email;

    private SubscriptionType subscriptionType;
}
