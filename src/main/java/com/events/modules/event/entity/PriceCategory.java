package com.events.modules.event.entity;

import com.events.common.abstraction.AuditableEntity;
import com.events.modules.event.enumeration.DefaultPriceCategoryEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "price_category")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE price_category SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class PriceCategory extends AuditableEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer availableTickets;

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
}

