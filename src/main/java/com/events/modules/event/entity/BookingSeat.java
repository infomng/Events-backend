package com.events.modules.event.entity;

import com.events.common.abstraction.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(
        name = "BOOKING_SEATS",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_booking_seat",
                columnNames = {"booking_id", "seat_id"}
        ),
        indexes = @Index(name = "idx_seat_id", columnList = "seat_id")
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
public class BookingSeat extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}

