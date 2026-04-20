package com.events.modules.cart.entity;

import com.events.common.abstraction.AuditableEntity;
import com.events.modules.cart.enumeration.CartStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private BigDecimal total;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CartStatusEnum status = CartStatusEnum.ACTIVE;

    @Builder.Default
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartItem> items = new ArrayList<>();

    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
        calculateTotal();
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
        calculateTotal();
    }

    public void clearItems() {
        items.clear();
        calculateTotal();
    }

    public void calculateTotal() {
        this.total = items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @PrePersist
    private void prePersist() {
        if (total == null) {
            calculateTotal();
        }
        if (status == null) {
            status = CartStatusEnum.ACTIVE;
        }
    }

    @PreUpdate
    private void preUpdate() {
        calculateTotal();
    }
}