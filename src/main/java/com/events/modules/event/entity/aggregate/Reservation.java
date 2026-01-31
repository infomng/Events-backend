package com.events.modules.event.entity.aggregate;

import com.events.common.abstraction.AuditableEntity;
import com.events.modules.event.enumeration.ReservationStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "RESERVATIONS",
        indexes = {
                @Index(name = "idx_seat_id", columnList = "seat_id"),
                @Index(name = "idx_expires_at", columnList = "expires_at"),
                @Index(name = "idx_user_id", columnList = "user_id")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
public class Reservation extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private String userId;

    private String sessionId;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ReservationStatusEnum status = ReservationStatusEnum.ACTIVE;

}
