package com.emmanuel.development.application.auth.service;

import com.emmanuel.development.application.auth.dto.AuthDTO;
import com.emmanuel.development.application.auth.entity.AppUser;
import com.emmanuel.development.application.auth.entity.CustomRole;
import com.emmanuel.development.application.auth.repository.AppUserRepository;
import com.emmanuel.development.application.auth.response.AuthResponse;
import com.emmanuel.development.application.enumeration.RoleEnum;
import com.emmanuel.development.application.exception.AlreadyExists;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

@Service @Setter
public class AuthService {

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

    private final AppUserRepository appUserRepository;

    private final PasswordEncoder passwordEncoder;

    private final SecurityContextRepository securityContextRepository;

    private final SecurityContextHolderStrategy securityContextHolderStrategy;

    private final AuthenticationManager authManager;

    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    private final SessionRegistry sessionRegistry;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authManager,
            FindByIndexNameSessionRepository<? extends Session> sessionRepository,
            SessionRegistry sessionRegistry,
            SecurityContextRepository securityContextRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.sessionRepository = sessionRepository;
        this.sessionRegistry = sessionRegistry;
        this.securityContextRepository = securityContextRepository;
        this.securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    }

    /**
     * Method responsible for registering an employee
     *
     * @param dto - an Object of AuthDTO
     * @throws AlreadyExists which represents a user not existing
     * @return String
     * **/
    public String register(AuthDTO dto) {
        String email = dto.email().trim();
        if (this.appUserRepository.principalExists(email) > 0) {
            throw new AlreadyExists(email + " exists");
        }

        var user = new AppUser();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setProfilePicture("");
        user.setLocked(true); // true if not locked
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true); // false for email validation
        user.addRole(new CustomRole(RoleEnum.USER));

        if (ADMIN_EMAIL.equals(email)) {
            user.addRole(new CustomRole(RoleEnum.ADMIN));
        }

        appUserRepository.save(user);
        return "Registered";
    }

    /**
     * Method responsible for logging in a user
     * For a better understanding, click the link below
     * <a href="https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html">...</a>
     * **/
    public AuthResponse login(
            AuthDTO dto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // Validate User credentials
        Authentication authentication = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(dto.email().trim(), dto.password()));

        // Validate session constraint is not exceeded
        validateMaxSession(authentication);

        // Create a new context
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        this.securityContextHolderStrategy.setContext(context);
        this.securityContextRepository.saveContext(context, request, response);

        // Build Response
        String list = authentication.getAuthorities().stream().map(String::valueOf).toList().toString();

        // Set custom cookie to replace using local storage to keep track of user logged in.
        Cookie cookie = new Cookie(IS_LOGGED_IN, URLEncoder.encode(list, StandardCharsets.UTF_8));
        cookie.setDomain(COOKIE_DOMAIN);
        cookie.setPath(COOKIE_PATH);
        cookie.setSecure(COOKIE_SECURE);
        cookie.setHttpOnly(false);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        response.addCookie(cookie);

        return new AuthResponse(dto.email());
    }

    /**
     * Method is responsible for validating user session is not exceeded. If it has been exceeded, the oldest valid
     * session is removed/ invalidated
     *
     * @param authentication of type Authentication
     * @return void - it just validates max session
     * */
    private void validateMaxSession(Authentication authentication) {
        // If max session is negative means unlimited session
        if (MAX_SESSION <= 0) {
            return;
        }

        var principal = (UserDetails) authentication.getPrincipal();
        List<SessionInformation> sessions = this.sessionRegistry.getAllSessions(principal, false);

        if (sessions.size() >= MAX_SESSION) {
            sessions
                    .stream() //
                    .min(Comparator.comparing(SessionInformation::getLastRequest)) // Gets the oldest session
                    .ifPresent(sessionInfo -> this.sessionRepository.deleteById(sessionInfo.getSessionId()));
        }
    }

}
