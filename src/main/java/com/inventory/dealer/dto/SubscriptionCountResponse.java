package com.inventory.dealer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionCountResponse {

    private Map<String, Long> counts;

    public Long getBasic() {
        return counts != null ? counts.get("BASIC") : null;
    }

    public Long getPremium() {
        return counts != null ? counts.get("PREMIUM") : null;
    }
}
