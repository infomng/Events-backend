package com.events.modules.cart.entity;

import com.events.common.abstraction.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CartItem extends AuditableEntity {

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private String eventName; // snapshot du nom de l'événement

    @Column(nullable = false)
    private UUID priceCategoryId;

    @Column(nullable = false)
    private String priceCategoryName; // snapshot du nom de la catégorie

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice; // snapshot prix unitaire au moment de l'ajout

    @Column(nullable = false)
    private BigDecimal totalPrice; // prix total calculé

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @PrePersist
    private void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    @PreUpdate
    private void updateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}