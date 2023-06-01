package com.emmanuel.development.application.auth.service;

import com.emmanuel.development.application.auth.dto.AuthDTO;
import com.emmanuel.development.application.auth.entity.AppUser;
import com.emmanuel.development.application.auth.entity.CustomRole;
import com.emmanuel.development.application.auth.repository.AppUserRepository;
import com.emmanuel.development.application.exception.AlreadyExists;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.emmanuel.development.application.enumeration.RoleEnum.ADMIN;
import static com.emmanuel.development.application.enumeration.RoleEnum.USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ActiveProfiles("dev")
@TestPropertySource(locations = "classpath:application-dev.properties")
class AuthServiceTest {

    @Value(value = "${custom.max-session}")
    private int MAX_SESSION;

    @Value(value = "${admin.email}")
    private String ADMIN_EMAIL;

    @Value(value = "${custom.cookie.frontend}")
    private String IS_LOGGED_IN;

    @Value(value = "${server.servlet.session.cookie.domain}")
    private String COOKIE_DOMAIN;

    @Value(value = "${server.servlet.session.cookie.max-age}")
    private int COOKIE_MAX_AGE;

    @Value(value = "${server.servlet.session.cookie.path}")
    private String COOKIE_PATH;

    @Value(value = "${server.servlet.session.cookie.secure}")
    private boolean COOKIE_SECURE;

    private AuthService authService;

    @Mock private AppUserRepository appUserRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @Mock private SecurityContextRepository securityContextRepository;

    @Mock private AuthenticationManager authManager;

    @Mock private FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Mock private SessionRegistry sessionRegistry;

    @BeforeEach
    void setUp() {
        this.authService = new AuthService(
                this.appUserRepository,
                this.passwordEncoder,
                this.authManager,
                this.sessionRepository,
                this.sessionRegistry,
                this.securityContextRepository
        );
        this.authService.setMAX_SESSION(MAX_SESSION);
        this.authService.setADMIN_EMAIL(ADMIN_EMAIL);
        this.authService.setCOOKIE_DOMAIN(COOKIE_DOMAIN);
        this.authService.setCOOKIE_PATH(COOKIE_PATH);
        this.authService.setCOOKIE_MAX_AGE(COOKIE_MAX_AGE);
        this.authService.setCOOKIE_SECURE(COOKIE_SECURE);
        this.authService.setIS_LOGGED_IN(IS_LOGGED_IN);
    }

    @Test
    void register() {
        // Given
        var dto = new AuthDTO(ADMIN_EMAIL, "password");

        // When
        when(this.appUserRepository.principalExists(anyString())).thenReturn(0);
        when(this.passwordEncoder.encode(anyString())).thenReturn(dto.password());
        when(this.appUserRepository.save(any(AppUser.class))).thenReturn(user());

        // Then
        assertEquals(this.authService.register(dto), "Registered");
        verify(this.appUserRepository, times(1)).save(any(AppUser.class));
    }

    @Test
    void register_with_already_existing_email() {
        // When
        when(this.appUserRepository.principalExists(anyString())).thenReturn(1);

        // Then
        assertThrows(AlreadyExists.class,
                () -> this.authService.register(new AuthDTO(ADMIN_EMAIL, "password")));
    }

    @Test
    void login() {
        // Given
        var dto = new AuthDTO(ADMIN_EMAIL, "password");
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Authentication authentication = Mockito.mock(Authentication.class);

        // When
        when(this.authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        // Then
        assertEquals(ADMIN_EMAIL, this.authService.login(dto, request, response).principal());
        verify(this.authManager).authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_with_non_existent_credentials() {
        // Given
        var dto = new AuthDTO("ade@ade.com", "password");
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        // When
        when(this.authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        // Then
        assertThrows(BadCredentialsException.class, () -> this.authService.login(dto, request, response));
    }

    private AppUser user() {
        var user = new AppUser();
        user.setEmail(ADMIN_EMAIL);
        user.setPassword("password");
        user.setEnabled(true);
        user.setLocked(true);
        user.setCredentialsNonExpired(true);
        user.setAccountNonExpired(true);
        user.addRole(new CustomRole(USER));
        user.addRole(new CustomRole(ADMIN));
        return user;
    }

}