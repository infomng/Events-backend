package com.events.modules.cart.entity;

import com.events.common.abstraction.AuditableEntity;
import com.events.modules.cart.enumeration.CartItemStatusEnum;
import com.events.modules.cart.enumeration.CartStatusEnum;
import com.events.modules.cart.exception.InvalidQuantityException;
import jakarta.persistence.*;
import lombok.*;
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
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private BigDecimal oldUnitPrice; // snapshot prix unitaire au moment de l'ajout

    @Column(nullable = false)
    private BigDecimal totalPrice; // prix total calculé

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CartItemStatusEnum status = CartItemStatusEnum.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @PrePersist
    private void calculateTotalPrice() {
        this.oldUnitPrice = this.unitPrice;
        calculatePrice();
    }

    @PreUpdate
    private void updateTotalPrice() {
        calculatePrice();
    }

    private void calculatePrice() {
        if (unitPrice != null && quantity != null) {
            totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public void updateQuantity(int newQuantity){
            if (newQuantity < 1){
                throw new InvalidQuantityException();
            }
            this.quantity = newQuantity;
    }
}