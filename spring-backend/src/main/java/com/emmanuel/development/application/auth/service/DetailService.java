package com.emmanuel.development.application.auth.service;

import com.emmanuel.development.application.auth.entity.details.AppUserDetails;
import com.emmanuel.development.application.auth.repository.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service(value = "detailService")
public class DetailService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    public DetailService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.appUserRepository
                .findByPrincipal(username) //
                .map(AppUserDetails::new) //
                .orElseThrow(() -> new UsernameNotFoundException(username + " not found"));
    }
}
