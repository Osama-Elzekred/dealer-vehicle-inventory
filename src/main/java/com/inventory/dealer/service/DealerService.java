package com.inventory.dealer.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.inventory.dealer.dto.CreateDealerRequest;
import com.inventory.dealer.dto.DealerResponse;
import com.inventory.dealer.dto.UpdateDealerRequest;

public interface DealerService {

    DealerResponse createDealer(CreateDealerRequest request);

    DealerResponse getDealerById(UUID id);

    Page<DealerResponse> getAllDealers(Pageable pageable);

    DealerResponse updateDealer(UUID id, UpdateDealerRequest request);

    void deleteDealer(UUID id);

    Map<String, Long> countBySubscription(boolean perTenant);
}
