package com.events.modules.event.entity;

import com.events.common.abstraction.AuditableEntity;
import com.events.modules.event.entity.aggregate.Image;
import com.events.modules.event.entity.aggregate.Seat;
import com.events.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import com.events.modules.event.enumeration.EventStatusEnum;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "EVENTS")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
public class Event extends AuditableEntity {

    private String name;
    private String description;
    private Boolean isPublic;
    private Boolean isFreeEntry;

    private Boolean hasInvitationCode;
    @Column(nullable = false)
    private String location;
    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private LocalDateTime startDate;
    @Column(nullable = false)
    private LocalDateTime endDate;
    private Integer totalTickets;

    private Integer availableTickets;
    private LocalDateTime ticketSalesStartDate;
    private LocalDateTime ticketSalesEndDate;
    private Double price;
    private Boolean hasSeats;
    @Enumerated(EnumType.STRING)
    private EventStatusEnum status;

    @ManyToMany
    @JoinTable(
            name = "EVENT_ATTENDEES",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> attendees;

    @ManyToMany
    @JoinTable(
            name = "EVENT_STAFF",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> staff;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Seat> seats;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Image> images = new HashSet<>();

    public boolean hasAvailableTickets() {
        return availableTickets != null && availableTickets > 0;
    }

    public boolean isTicketSalesActive() {
        LocalDateTime now = LocalDateTime.now();
        return (ticketSalesStartDate == null || now.isAfter(ticketSalesStartDate)) &&
               (ticketSalesEndDate == null || now.isBefore(ticketSalesEndDate));
    }

}
