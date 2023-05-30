package com.emmanuel.development.application.auth.entity.details;

import com.emmanuel.development.application.auth.entity.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;

public record AppUserDetails(AppUser appUser) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.appUser.getAuthorities();
    }

    @Override
    public String getPassword() {
        return this.appUser.getPassword();
    }

    @Override
    public String getUsername() {
        return this.appUser.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.appUser.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.appUser.isLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.appUser.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return this.appUser.isEnabled();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppUserDetails that)) return false;
        return Objects.equals(appUser, that.appUser);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.appUser);
    }

    @Override
    public String toString() {
        return this.appUser.toString();
    }

}
