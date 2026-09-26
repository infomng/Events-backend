package com.events.modules.cart.entity;

import com.events.common.abstraction.AuditableEntity;
import com.events.modules.cart.enumeration.CartItemStatusEnum;
import com.events.modules.cart.exception.NotEnoughQuantityException;
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
    @Builder.Default
    private Integer quantity = 0;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal oldUnitPrice = BigDecimal.ZERO; // snapshot prix unitaire au moment de l'ajout

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal totalPrice = BigDecimal.ZERO; // prix total calculé

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CartItemStatusEnum status = CartItemStatusEnum.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    private void calculatePrice() {
        if (unitPrice != null && quantity != null) {
            totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public void updateQuantity(int newQuantity){
            if (newQuantity < 0){
                throw new NotEnoughQuantityException();
            }
            this.quantity = newQuantity;
            calculatePrice();
    }
}