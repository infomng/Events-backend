package com.events.modules.event.entity.aggregate;

import com.events.common.abstraction.AuditableEntity;
import com.events.modules.event.entity.Event;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "IMAGES")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor @SuperBuilder
public class Image extends AuditableEntity {
//        @Column(nullable = false)
//        private String filename;

        @Column(nullable = false)
        private String url;

//        @Column(nullable = false)
//        private String contentType; // image/png, image/jpeg

//        @Column()
//        private Long size; // en octets
//        @Column()
//        private Integer width;
//        @Column()
//        private Integer height;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "event_id")
        private Event event;

}

