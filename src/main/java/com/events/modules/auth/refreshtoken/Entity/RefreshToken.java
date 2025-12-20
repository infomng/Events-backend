package com.events.modules.auth.refreshtoken.Entity;

import com.events.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(columnDefinition = "TEXT")
    private String token;

    private Instant expirationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    public boolean isExpired() {
        return Instant.now().isAfter(expirationDate);
    }
}
