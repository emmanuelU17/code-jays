package com.emmanuel.development.application.auth.service;

import com.emmanuel.development.application.auth.repository.AppUserRepository;
import com.emmanuel.development.application.image.response.ImageResponse;
import com.emmanuel.development.application.image.service.FilesUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.springframework.http.HttpStatus.*;

@Service
public class AccountService {

    private final AppUserRepository appUserRepository;

    private final PasswordEncoder passwordEncoder;

    private final FilesUtils filesUtils;

    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    private final SessionRegistry sessionRegistry;

    public AccountService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            FilesUtils filesUtils,
            FindByIndexNameSessionRepository<? extends Session> sessionRepository,
            SessionRegistry sessionRegistry
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.filesUtils = filesUtils;
        this.sessionRepository = sessionRepository;
        this.sessionRegistry = sessionRegistry;
    }

    /** Method gets user profile upon signing in */
    public ResponseEntity<?> get_profile_picture() {
        ImageResponse imageResponse;
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        String picture_path = this.appUserRepository.getProfilePicture(principal) == null
                ? "" : this.appUserRepository.getProfilePicture(principal);

        if (picture_path.isEmpty()) {
            return new ResponseEntity<>("No profile photo", OK);
        }

        try {
            Path path = Path.of(picture_path);
            imageResponse = new ImageResponse(
                    path.getFileName().toString(),
                    Files.probeContentType(path),
                    Files.readAllBytes(path)
            );
        } catch (IOException e) {
            return new ResponseEntity<>("Error retrieving user image", INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(imageResponse, OK);
    }

    /** Method uploads file to folder and path to user table */
    public ResponseEntity<?> upload_profile_picture(MultipartFile file) {
        Path uploadPath = Paths.get("uploads");

        if (!this.filesUtils.exists(uploadPath)) {
            try {
                this.filesUtils.create_directory(uploadPath);
            } catch (IOException ex) {
                return new ResponseEntity<>(
                        "Server error please try again " + ex.getMessage(), INTERNAL_SERVER_ERROR);
            }
        }

        StringBuilder sb = new StringBuilder();
        try (InputStream inputStream = file.getInputStream()) {
            String name = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            sb.append(this.filesUtils.save_to_profile_folder(inputStream, uploadPath, name));
        } catch (IOException ex) {
            return new ResponseEntity<>("Error saving image " + ex.getMessage(), BAD_REQUEST);
        }

        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        this.appUserRepository.update_profile_picture(principal, sb.toString());
        return new ResponseEntity<>("Uploaded", OK);
    }

    /**  Private Route to reset a users password  */
    public String password_reset(String password) {
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        this.appUserRepository.updatePassword(principal, passwordEncoder.encode(password));
        delete_sessions(SecurityContextHolder.getContext().getAuthentication());
        return "Password changed!";
    }

    /** Delete all user session from DB */
    private void delete_sessions(Authentication authentication) {
        var principal = (UserDetails) authentication.getPrincipal();
        this.sessionRegistry.getAllSessions(principal, false)
                .forEach((session) -> this.sessionRepository.deleteById(session.getSessionId()));
    }

}
