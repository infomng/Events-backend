package com.events.modules.booking.entity;

import com.events.common.abstraction.AuditableEntity;
import com.events.modules.event.entity.aggregate.BookingSeat;
import com.events.modules.event.enumeration.BookingStatusEnum;
import com.events.modules.event.enumeration.PaymentStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(
        name = "BOOKINGS",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_booking_reference", columnList = "booking_reference"),
                @Index(name = "idx_status", columnList = "status")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
public class Booking extends AuditableEntity {

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private BookingStatusEnum status = BookingStatusEnum.PENDING;

    private String paymentId;

    @Enumerated(EnumType.STRING)
    private PaymentStatusEnum paymentStatus = PaymentStatusEnum.PENDING;

    @Column(nullable = false, unique = true)
    private String bookingReference;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime confirmedAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private Set<BookingSeat> bookingSeats;

}

