package com.emmanuel.development.application.auth.service;

import com.emmanuel.development.application.auth.entity.AppUser;
import com.emmanuel.development.application.auth.entity.CustomRole;
import com.emmanuel.development.application.auth.entity.details.AppUserDetails;
import com.emmanuel.development.application.auth.repository.AppUserRepository;
import com.emmanuel.development.application.image.service.FilesUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;

import static com.emmanuel.development.application.enumeration.RoleEnum.ADMIN;
import static com.emmanuel.development.application.enumeration.RoleEnum.USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ActiveProfiles("dev")
@TestPropertySource(locations = "classpath:application-dev.properties")
class AccountServiceTest {

    private AccountService accountService;

    @Mock private AppUserRepository appUserRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @Mock private FilesUtils filesUtils;

    @Mock private FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Mock private SessionRegistry sessionRegistry;

    @BeforeEach
    void setUp() {
        this.accountService = new AccountService(
                appUserRepository, passwordEncoder, filesUtils, sessionRepository, sessionRegistry
        );
    }

    // For this test to pass, uploads folder has to be created.
    @Test
    void get_profile_picture() {
        // Given
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        // When
        doReturn(user().getUsername()).when(auth).getName();
        doReturn(auth).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        doReturn("uploads\\image1.jpeg").when(this.appUserRepository).getProfilePicture(anyString());

        // Then
        assertEquals(OK, this.accountService.get_profile_picture().getStatusCode());
    }

    @Test
    void upload_profile_picture() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image1.jpeg",
                "image/jpeg",
                "Test image content".getBytes()
        );
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        // When
        when(this.filesUtils.exists(any(Path.class))).thenReturn(true);
        when(this.filesUtils.save_to_profile_folder(any(InputStream.class), any(Path.class), anyString()))
                .thenReturn("profile_uploads/image1.jpg");

        doReturn(user().getUsername()).when(auth).getName();
        doReturn(auth).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        // Then
        var response = this.accountService.upload_profile_picture(file);
        assertEquals("Uploaded", response.getBody());
        assertEquals(OK, response.getStatusCode());
        verify(this.filesUtils, times(1))
                .save_to_profile_folder(any(InputStream.class), any(Path.class), anyString());
        verify(this.appUserRepository, times(1))
                .update_profile_picture(anyString(), anyString());
    }

    @Test
    void error_creating_folder() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image1.jpeg",
                "image/jpeg",
                "Test image content".getBytes()
        );

        // When
        when(this.filesUtils.exists(any(Path.class))).thenReturn(false);
        doThrow(IOException.class).when(this.filesUtils).create_directory(any(Path.class));

        // Then
        assertEquals(INTERNAL_SERVER_ERROR, this.accountService.upload_profile_picture(file).getStatusCode());
    }

    @Test
    void client_error_uploading_image() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                null,
                null,
                "".getBytes()
        );

        // When
        when(this.filesUtils.exists(any(Path.class))).thenReturn(false);
        doThrow(IOException.class).when(this.filesUtils)
                .save_to_profile_folder(any(InputStream.class), any(Path.class), anyString());

        // Then
        assertEquals(BAD_REQUEST, this.accountService.upload_profile_picture(file).getStatusCode());
    }

    @Test
    void password_reset() {
        // Given
        String password = "password";
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        // When
        when(auth.getPrincipal()).thenReturn(user());
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(this.passwordEncoder.encode(anyString())).thenReturn(password);
        when(this.sessionRegistry.getAllSessions(any(UserDetails.class), anyBoolean()))
                .thenReturn(new ArrayList<>());

        // Then
        assertEquals("Password changed!", this.accountService.password_reset(password));
    }

    private UserDetails user() {
        var user = new AppUser();
        user.setEmail("admin@gmail.com");
        user.setPassword("password");
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setLocked(true);
        user.setProfilePicture("profile_uploads/image1.jpeg");
        user.setCredentialsNonExpired(true);
        user.addRole(new CustomRole(USER));
        user.addRole(new CustomRole(ADMIN));
        return new AppUserDetails(user);
    }

}