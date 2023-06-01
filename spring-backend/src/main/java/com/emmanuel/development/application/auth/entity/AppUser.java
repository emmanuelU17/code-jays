package com.emmanuel.development.application.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.EAGER;

@Entity @Table(name = "app_user") @NoArgsConstructor @Getter @Setter
public class AppUser implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "profile_picture_path")
    private String profilePicture;

    @Column(name = "account_enabled")
    private boolean enabled;

    @Column(name = "credentials_expired")
    private boolean credentialsNonExpired;

    @Column(name = "account_expired")
    private boolean accountNonExpired;

    @Column(name = "account_locked")
    private boolean locked;

    @OneToOne(cascade = ALL)
    @JoinColumn(name = "password_token_id", referencedColumnName = "password_reset_token_id")
    private PasswordResetToken passwordToken;

    @JsonIgnore
    @OneToMany(cascade = {PERSIST, MERGE, REMOVE}, fetch = EAGER, mappedBy = "appUser", orphanRemoval = true)
    private Set<CustomRole> roles = new HashSet<>();

    public void addRole(CustomRole role) {
        this.roles.add(role);
        role.setAppUser(this);
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this
                .roles
                .stream() //
                .map(role -> new SimpleGrantedAuthority(role.getRoleEnum().toString()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppUser user)) return false;
        return isEnabled() == user.isEnabled()
                && isCredentialsNonExpired() == user.isCredentialsNonExpired()
                && isAccountNonExpired() == user.isAccountNonExpired()
                && isLocked() == user.isLocked()
                && Objects.equals(getUserId(), user.getUserId())
                && Objects.equals(getEmail(), user.getEmail())
                && Objects.equals(getPassword(), user.getPassword())
                && Objects.equals(getProfilePicture(), user.getProfilePicture())
                && Objects.equals(getPasswordToken(), user.getPasswordToken())
                && Objects.equals(getRoles(), user.getRoles());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getUserId(),
                getEmail(),
                getPassword(),
                getProfilePicture(),
                isEnabled(),
                isCredentialsNonExpired(),
                isAccountNonExpired(),
                isLocked(),
                getPasswordToken(),
                getRoles()
        );
    }
}
