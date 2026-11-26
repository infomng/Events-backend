package com.events.modules.booking.entity;

import com.events.common.abstraction.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Ligne d'une réservation : une quantité de billets d'une catégorie de prix, au prix figé à la commande.
 */
@Entity
@Table(name = "BOOKING_ITEMS", indexes = @Index(name = "idx_booking_item_booking", columnList = "booking_id"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
public class BookingItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false)
    private UUID priceCategoryId;

    @Column(nullable = false)
    private String priceCategoryName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
}
