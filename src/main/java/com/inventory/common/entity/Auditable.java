package com.inventory.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/**
 * Embeddable component for automatic audit timestamp management.
 *
 * Uses Spring Data JPA auditing to automatically populate:
 * - createdAt: Set once on entity creation
 * - updatedAt: Updated on every entity modification
 *
 * Entities using this component must be annotated with:
 * @EntityListeners(AuditingEntityListener.class)
 */
@Embeddable
@Getter
public class Auditable {

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
