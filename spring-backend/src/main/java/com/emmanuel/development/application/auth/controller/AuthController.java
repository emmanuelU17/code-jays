package com.emmanuel.development.application.auth.controller;

import com.emmanuel.development.application.auth.dto.AuthDTO;
import com.emmanuel.development.application.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController @RequestMapping(path = "/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Public route called when registering a user **/
    @PostMapping(path = "/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthDTO dto) {
        return ResponseEntity.status(CREATED).body(this.authService.register(dto));
    }

    /** Public API that allows an employee to login **/
    @PostMapping(path = "/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody AuthDTO dto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return new ResponseEntity<>(this.authService.login(dto, request, response), OK);
    }

}
