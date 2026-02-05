package com.events.modules.country.entity;

import com.events.common.abstraction.AuditableEntity;
import com.events.modules.country.enumeration.PaysEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "countries",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "name"),
                @UniqueConstraint(columnNames = "code")
        })
@SQLDelete(sql = "UPDATE countries SET is_active = false WHERE id = ?")
@SQLRestriction("is_active = true")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Country extends AuditableEntity {

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false, length = 2)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaysEnum paysEnum;
}
