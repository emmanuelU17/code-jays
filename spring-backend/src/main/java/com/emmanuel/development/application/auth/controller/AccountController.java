package com.emmanuel.development.application.auth.controller;

import com.emmanuel.development.application.auth.service.AccountService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.HttpStatus.OK;

/** All routes are private. User has to be authenticated to access */
@RestController @RequestMapping(path = "api/v1/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Retrieves user profile picture
     *
     * @return ResponseEntity of type ImageResponse
     * */
    @GetMapping(produces = "application/json")
    public ResponseEntity<?> get_profile_picture() {
        return new ResponseEntity<>(this.accountService.get_profile_picture(), OK);
    }

    /**
     * Uploads user profile to the DB
     *
     * @param file - accepts a MultipartFile from UI
     * @return ResponseEntity of type String
     * */
    @PostMapping(path = "/upload")
    public ResponseEntity<?> upload_profile_picture(@Param(value = "file") MultipartFile file) {
        return new ResponseEntity<>(this.accountService.upload_profile_picture(file), OK);
    }

    /**
     * Allows resetting a user password.
     *
     * @param password - accepts a password from UI
     * @return ResponseEntity of type String
     * */
    @PutMapping
    public ResponseEntity<?> password_reset(@Param(value = "password") String password) {
        return new ResponseEntity<>(this.accountService.password_reset(password), OK);
    }

}
