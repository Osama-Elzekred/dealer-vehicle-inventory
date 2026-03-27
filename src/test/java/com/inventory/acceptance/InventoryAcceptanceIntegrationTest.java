package com.inventory.acceptance;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class InventoryAcceptanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void missingTenantHeaderShouldReturn400() throws Exception {
        String token = loginAndGetToken("tenant-user-missing-header", "tenant-1", "TENANT_USER");

        mockMvc.perform(get("/dealers")
                .header("Authorization", bearer(token)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Missing required header: X-Tenant-Id"));
    }

    @Test
    void crossTenantAccessShouldReturn403() throws Exception {
        String token = loginAndGetToken("tenant-user-cross", "tenant-1", "TENANT_USER");

        mockMvc.perform(get("/dealers")
                .header("Authorization", bearer(token))
                .header("X-Tenant-Id", "tenant-2"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void subscriptionFilterShouldReturnOnlyPremiumVehiclesInCallerTenant() throws Exception {
        String token = loginAndGetToken("tenant-user-subscription", "tenant-1", "TENANT_USER");

        mockMvc.perform(get("/vehicles")
                .param("subscription", "PREMIUM")
                .header("Authorization", bearer(token))
                .header("X-Tenant-Id", "tenant-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(4))
            .andExpect(jsonPath("$.content[0].tenantId").value("tenant-1"))
            .andExpect(jsonPath("$.content[0].dealerSubscription").value("PREMIUM"));
    }

    @Test
    void adminCountsShouldRequireGlobalAdmin() throws Exception {
        String userToken = loginAndGetToken("tenant-user-admin-check", "tenant-1", "TENANT_USER");

        mockMvc.perform(get("/admin/dealers/countBySubscription")
                .header("Authorization", bearer(userToken)))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCountsShouldAllowGlobalAdmin() throws Exception {
        String adminToken = loginAndGetToken("global-admin", "system", "GLOBAL_ADMIN");

        mockMvc.perform(get("/admin/dealers/countBySubscription")
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.BASIC").exists())
            .andExpect(jsonPath("$.PREMIUM").exists());
    }

    @Test
    void dealerCrudShouldWorkWithPaginationAndSort() throws Exception {
        String token = loginAndGetToken("tenant-user-dealer-crud", "tenant-1", "TENANT_USER");
        String unique = UUID.randomUUID().toString().substring(0, 8);

        String createdDealerId = createDealer(token, "Dealer " + unique, "dealer-" + unique + "@example.com", "BASIC");

        mockMvc.perform(get("/dealers/{id}", createdDealerId)
                .header("Authorization", bearer(token))
                .header("X-Tenant-Id", "tenant-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(createdDealerId));

        mockMvc.perform(get("/dealers")
                .param("page", "0")
                .param("size", "5")
                .param("sort", "name,asc")
                .header("Authorization", bearer(token))
                .header("X-Tenant-Id", "tenant-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(5));

        mockMvc.perform(patch("/dealers/{id}", createdDealerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"subscriptionType\":\"PREMIUM\"}")
                .header("Authorization", bearer(token))
                .header("X-Tenant-Id", "tenant-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.subscriptionType").value("PREMIUM"));

        mockMvc.perform(delete("/dealers/{id}", createdDealerId)
                .header("Authorization", bearer(token))
                .header("X-Tenant-Id", "tenant-1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void vehicleCrudShouldWorkWithRequiredFilters() throws Exception {
        String token = loginAndGetToken("tenant-user-vehicle-crud", "tenant-1", "TENANT_USER");
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String createdDealerId = createDealer(token, "Vehicle Dealer " + unique, "vehicle-dealer-" + unique + "@example.com", "PREMIUM");

        String createdVehicleId = createVehicle(token, createdDealerId, "Model-" + unique, new BigDecimal("12345.67"), "AVAILABLE");

        mockMvc.perform(get("/vehicles/{id}", createdVehicleId)
                .header("Authorization", bearer(token))
                .header("X-Tenant-Id", "tenant-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(createdVehicleId));

        mockMvc.perform(get("/vehicles")
                .param("model", "Model-")
                .param("status", "AVAILABLE")
                .param("priceMin", "10000")
                .param("priceMax", "13000")
                .param("page", "0")
                .param("size", "5")
                .param("sort", "price,desc")
                .header("Authorization", bearer(token))
                .header("X-Tenant-Id", "tenant-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(5));

        mockMvc.perform(patch("/vehicles/{id}", createdVehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"SOLD\"}")
                .header("Authorization", bearer(token))
                .header("X-Tenant-Id", "tenant-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SOLD"));

        mockMvc.perform(delete("/vehicles/{id}", createdVehicleId)
                .header("Authorization", bearer(token))
                .header("X-Tenant-Id", "tenant-1"))
            .andExpect(status().isNoContent());

        mockMvc.perform(delete("/dealers/{id}", createdDealerId)
                .header("Authorization", bearer(token))
                .header("X-Tenant-Id", "tenant-1"))
            .andExpect(status().isNoContent());
    }

    private String loginAndGetToken(String username, String tenantId, String role) throws Exception {
        String loginBody = String.format("{\"username\":\"%s\",\"tenantId\":\"%s\",\"role\":\"%s\"}",
                username,
                tenantId,
                role);

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode node = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = node.get("token").asText();
        assertThat(token).isNotBlank();
        return token;
    }

    private String createDealer(String token, String name, String email, String subscriptionType) throws Exception {
        String body = String.format("{\"name\":\"%s\",\"email\":\"%s\",\"subscriptionType\":\"%s\"}",
                name,
                email,
                subscriptionType);

        MvcResult result = mockMvc.perform(post("/dealers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header("Authorization", bearer(token))
                .header("X-Tenant-Id", "tenant-1"))
            .andExpect(status().isCreated())
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private String createVehicle(String token, String dealerId, String model, BigDecimal price, String status) throws Exception {
        String body = String.format("{\"dealerId\":\"%s\",\"model\":\"%s\",\"price\":%s,\"status\":\"%s\"}",
                dealerId,
                model,
                price,
                status);

        MvcResult result = mockMvc.perform(post("/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header("Authorization", bearer(token))
                .header("X-Tenant-Id", "tenant-1"))
            .andExpect(status().isCreated())
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
