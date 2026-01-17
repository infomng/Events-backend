package com.events.modules.ticket.entity;

import com.events.common.abstraction.AuditableEntity;
import com.events.modules.event.entity.Event;
import com.events.modules.ticket.enumeration.TicketStatusEnum;
import com.events.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


import java.util.UUID;

@Entity
@Table(name = "TICKETS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Ticket extends AuditableEntity {

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String ticketCode;

    @Column(nullable = false)
    private Double price;

    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatusEnum status;

    @Lob
    private byte[] qrCode;
}
