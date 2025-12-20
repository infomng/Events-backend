package com.events.modules.event.entity;

import com.events.common.abstraction.AuditableEntity;
import com.events.modules.event.enumeration.SeatStatusEnum;
import com.events.modules.event.enumeration.SeatTypeEnum;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(
        name = "SEATS",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_event_seat",
                columnNames = {"event_id", "seat_number"}
        ),
        indexes = {
                @Index(name = "idx_event_status", columnList = "event_id, status"),
                @Index(name = "idx_reserved_until", columnList = "reserved_until")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
public class Seat extends AuditableEntity{

    @Column(nullable = false, length = 20)
    private String seatNumber;

    private String section;

    @Column(name = "row_number")
    private String rowNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatTypeEnum seatType = SeatTypeEnum.REGULAR;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatusEnum status = SeatStatusEnum.AVAILABLE;

    @Version
    private Long version;

    private String reservedBy;

    private LocalDateTime reservedUntil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;
}

