package com.emmanuel.development.application.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;

@Component(value = "customLogoutHandler")
public class CustomLogoutHandler implements LogoutHandler {

    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    public CustomLogoutHandler(FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * Method responsible for deleting user session post logout
     *
     * @param request
     * @param response
     * @param authentication
     * @return void
     * */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String id = request.getSession(false).getId();
        if (id != null && this.sessionRepository.findById(id) != null) {
            this.sessionRepository.deleteById(id);
        }
    }

}