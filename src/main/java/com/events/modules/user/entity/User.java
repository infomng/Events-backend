package com.events.modules.user.entity;

import com.events.common.abstraction.AuditableEntity;
import com.events.modules.country.entity.Country;
import com.events.modules.event.entity.Event;
import com.events.modules.user.enumeration.RoleEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "USERS")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class User extends AuditableEntity {

    private String fullName;
    private String email;
    private String password;
    private String verificationToken;
    private String resetPasswordToken;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isVerified = false;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isEnabled = true;

    @Builder.Default()
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isAccountNonExpired = true;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isAccountNonLocked = true;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isCredentialsNonExpired = true;

    @Builder.Default
    @JsonIgnore
    @OneToMany(mappedBy = "organizer", fetch = FetchType.LAZY)
    private Set<Event> organizedEvents = new HashSet<>();

    @ManyToMany(mappedBy = "attendees",  fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Event> eventsAttending;

    @ManyToMany(mappedBy = "staff", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Event> eventsStaffing;

    @Builder.Default
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "USER_FAVORITE_EVENTS",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private Set<Event> favoriteEvents = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private RoleEnum role;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;
}

