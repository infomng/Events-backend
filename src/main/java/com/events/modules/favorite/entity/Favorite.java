package com.events.modules.favorite.entity;


import com.events.common.abstraction.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "FAVORITE",uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","event_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Favorite extends AuditableEntity {
    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private UUID userId;

    public static Favorite create(UUID eventId, UUID userId) {
        return new Favorite(eventId, userId);
    }

    public static Favorite get( UUID eventId, UUID userId) {
        return new Favorite(eventId, userId);
    }
}
