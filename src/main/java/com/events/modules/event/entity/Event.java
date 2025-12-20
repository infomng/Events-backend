package com.events.modules.event.entity;

import com.events.common.abstraction.AuditableEntity;
import com.events.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
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
    private Boolean isFree;
    private Boolean isFreeEntry;

    private String invitationCode;
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
    private Double price;
    private Boolean hasSits;
    private LocalDateTime ticketSalesStartDate;

    private LocalDateTime ticketSalesEndDate;
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

    public boolean hasAvailableTickets() {
        return availableTickets != null && availableTickets > 0;
    }

    public boolean isTicketSalesActive() {
        LocalDateTime now = LocalDateTime.now();
        return (ticketSalesStartDate == null || now.isAfter(ticketSalesStartDate)) &&
               (ticketSalesEndDate == null || now.isBefore(ticketSalesEndDate));
    }

}
