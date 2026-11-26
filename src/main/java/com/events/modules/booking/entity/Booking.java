package com.events.modules.booking.entity;

import com.events.common.abstraction.AuditableEntity;
import com.events.modules.event.entity.aggregate.BookingSeat;
import com.events.modules.event.enumeration.BookingStatusEnum;
import com.events.modules.event.enumeration.PaymentStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "BOOKINGS",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_booking_reference", columnList = "booking_reference"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_booking_status_expires", columnList = "status, expires_at")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
public class Booking extends AuditableEntity {

    @Column(nullable = false)
    private UUID eventId;

    /** Snapshot du nom de l'événement au moment de la commande. */
    private String eventName;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private BookingStatusEnum status = BookingStatusEnum.PENDING;

    private String paymentId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private PaymentStatusEnum paymentStatus = PaymentStatusEnum.PENDING;

    @Column(nullable = false, unique = true)
    private String bookingReference;

    /** Date limite de paiement : au-delà, les places bloquées sont libérées. */
    private LocalDateTime expiresAt;

    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookingItem> items = new HashSet<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private Set<BookingSeat> bookingSeats;

    @PrePersist
    private void generateBookingReference() {
        if (bookingReference == null) {
            bookingReference = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

    public void addItem(BookingItem item) {
        items.add(item);
        item.setBooking(this);
    }

    public boolean isAwaitingPayment(LocalDateTime now) {
        return status == BookingStatusEnum.PENDING && (expiresAt == null || expiresAt.isAfter(now));
    }

    public void confirm(String paymentId) {
        this.status = BookingStatusEnum.CONFIRMED;
        this.paymentStatus = PaymentStatusEnum.SUCCESS;
        this.paymentId = paymentId;
        this.expiresAt = null;
    }
}
