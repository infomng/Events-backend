package com.events.modules.event.entity.aggregate;

import com.events.common.abstraction.AuditableEntity;
import com.events.modules.event.entity.Event;
import com.events.modules.event.enumeration.DefaultPriceCategoryEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "price_categories",
        // Un nom de catégorie est unique par événement, pas dans toute la base
        uniqueConstraints = @UniqueConstraint(name = "uk_price_category_event_name", columnNames = {"event_id", "name"}))
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE price_categories SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class PriceCategory extends AuditableEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer availableTickets;

    @Column
    @Builder.Default
    private Integer soldTickets = 0;

    @Column(nullable = false)
    private Integer totalTickets;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Builder.Default
    private boolean deleted = Boolean.FALSE;

    @PrePersist
    private void initializeAvailableTickets() {
        if(DefaultPriceCategoryEnum.FREE.name().equals(name)) {
            price = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    private void updateAvailableTickets() {
        availableTickets = totalTickets - soldTickets;
    }
}

