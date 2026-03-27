package com.inventory.vehicle.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import com.inventory.dealer.entity.Dealer;
import com.inventory.dealer.entity.Dealer_;
import com.inventory.dealer.entity.SubscriptionType;
import com.inventory.vehicle.dto.VehicleFilterRequest;
import com.inventory.vehicle.entity.Vehicle;
import com.inventory.vehicle.entity.VehicleStatus;
import com.inventory.vehicle.entity.Vehicle_;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public final class VehicleSpecification {

    private static final char ESCAPE_CHAR = '\\';

    private VehicleSpecification() {
    }

    public static Specification<Vehicle> withTenantId(String tenantId) {
        return (root, query, cb) -> cb.equal(root.get(Vehicle_.tenantId), tenantId);
    }

    public static Specification<Vehicle> withModel(String model) {
        return (root, query, cb) -> {
            if (model == null || model.isBlank()) {
                return null;
            }
            String escaped = escapeLikePattern(model.toLowerCase());
            return cb.like(cb.lower(root.get(Vehicle_.model)), "%" + escaped + "%", ESCAPE_CHAR);
        };
    }

    public static Specification<Vehicle> withStatus(VehicleStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return null;
            }
            return cb.equal(root.get(Vehicle_.status), status);
        };
    }

    public static Specification<Vehicle> withPriceMin(BigDecimal priceMin) {
        return (root, query, cb) -> {
            if (priceMin == null) {
                return null;
            }
            return cb.greaterThanOrEqualTo(root.get(Vehicle_.price), priceMin);
        };
    }

    public static Specification<Vehicle> withPriceMax(BigDecimal priceMax) {
        return (root, query, cb) -> {
            if (priceMax == null) {
                return null;
            }
            return cb.lessThanOrEqualTo(root.get(Vehicle_.price), priceMax);
        };
    }

    public static Specification<Vehicle> withDealerSubscription(SubscriptionType subscription) {
        return (root, query, cb) -> {
            if (subscription == null) {
                return null;
            }
            Join<Vehicle, Dealer> dealerJoin = root.join(Vehicle_.dealer, JoinType.INNER);
            return cb.equal(dealerJoin.get(Dealer_.subscriptionType), subscription);
        };
    }

    public static Specification<Vehicle> withFilters(String tenantId, VehicleFilterRequest filter) {
        return (root, query, cb) -> {
            query.distinct(true);
            return Specification.where(withTenantId(tenantId))
                    .and(withModel(filter.getModel()))
                    .and(withStatus(filter.getStatus()))
                    .and(withPriceMin(filter.getPriceMin()))
                    .and(withPriceMax(filter.getPriceMax()))
                    .and(withDealerSubscription(filter.getSubscription()))
                    .toPredicate(root, query, cb);
        };
    }

    private static String escapeLikePattern(String pattern) {
        return pattern
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
