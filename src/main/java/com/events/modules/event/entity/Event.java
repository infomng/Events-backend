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
@Table(name = "EVENTS",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {
                        "organizer_id",
                        "name",
                        "start_date",
                        "location"
                }
        ))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder

public class Event extends AuditableEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Boolean isPublic;

    @Column(nullable = false)
    private Boolean hasInvitationCode;

    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private LocalDateTime ticketSalesStartDate;
    private LocalDateTime ticketSalesEndDate;

    @Column(nullable = false)
    private Boolean hasSeats;

    @Column(nullable = false)
    private Boolean isTicketSalesActive;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Setter(AccessLevel.NONE)
    private Integer totalTickets;

    @Setter(AccessLevel.NONE)
    private Integer availableTickets;

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

    @Builder.Default
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Image> images = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PriceCategory> priceCategories = new HashSet<>();

    @PrePersist
    @PreUpdate
    private void compute() {
        computeAvailableTickets();
        computeTotalTickets();
        isTicketSalesActive();

    }

    private void computeAvailableTickets() {
        this.availableTickets = priceCategories.stream()
                .mapToInt(PriceCategory::getAvailableTickets)
                .sum();
    }

    private void computeTotalTickets() {
        this.totalTickets = priceCategories.stream()
                .mapToInt(PriceCategory::getTotalTickets)
                .sum();
    }

    private void isTicketSalesActive() {
        LocalDateTime now = LocalDateTime.now();
        this.isTicketSalesActive = (ticketSalesStartDate == null || now.isAfter(ticketSalesStartDate)) &&
                (ticketSalesEndDate == null || now.isBefore(ticketSalesEndDate));
    }



}
