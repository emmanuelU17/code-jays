package com.emmanuel.development.application.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Table @Entity @NoArgsConstructor @Getter @Setter
public class PasswordResetToken implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "password_reset_token_id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expire_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "token", nullable = false)
    private String token;

    @OneToOne(mappedBy = "passwordToken")
    private AppUser appUser;

    public boolean isExpired() {
        return getExpiresAt().isBefore(LocalDateTime.now());
    }

}
