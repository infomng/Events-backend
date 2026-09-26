package com.events.modules.cart.entity;

import com.events.common.abstraction.AuditableEntity;
import com.events.modules.cart.exception.CartItemNotFoundException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Cart extends AuditableEntity {

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false)
    private Integer totalItems;

    @Column(nullable = false)
    private BigDecimal totalPrice;


    @Builder.Default
    @NotNull
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<CartItem> items = new HashSet<>();

    @PrePersist
    private void prePersist() {
            countTotalItems();
            calculateTotalPrice();
    }

    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
        calculateTotalPrice();
    }

    public void updateItem(CartItem item, int newQuantity) {
        item.updateQuantity(newQuantity);
        calculateTotalPrice();
    }

    private void countTotalItems() {
        this.totalItems = items.size();
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
        this.totalItems = items.size();
        calculateTotalPrice();
    }

    public void clearItems() {
        for (CartItem item : items) {
            item.setCart(null);
        }
        items.clear();
        countTotalItems();
        calculateTotalPrice();
    }

    public void calculateTotalPrice() {
        this.totalPrice = items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}