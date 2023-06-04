package com.emmanuel.development.application.auth.controller;

import com.emmanuel.development.application.auth.dto.AuthDTO;
import com.emmanuel.development.application.auth.dto.ResetDTO;
import com.emmanuel.development.application.auth.repository.AppUserRepository;
import com.emmanuel.development.application.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@TestPropertySource(locations = "classpath:application-dev.properties")
class AccountControllerTest {

    @Value(value = "${admin.email}")
    private String ADMIN_EMAIL;

    @Value(value = "${custom.cookie.name}")
    private String COOKIE_NAME;

    @Autowired private MockMvc MOCK_MVC;

    @Autowired private AuthService authService;

    @Autowired private AppUserRepository appUserRepository;

    @Container
    private static final MySQLContainer<?> container = new MySQLContainer<>("mysql:latest")
            .withDatabaseName("code_jays_db")
            .withUsername("password")
            .withPassword("password");

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }

    @BeforeEach
    void setUp() {
        authService.register(new AuthDTO(ADMIN_EMAIL, "password"));
    }

    @AfterEach
    void tearDown() {
        this.appUserRepository.deleteAll();
    }

    @Test
    @Order(1)
    void get_profile_picture() throws Exception {
        // Login
        MvcResult mvcResult = this.MOCK_MVC
                .perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new AuthDTO(ADMIN_EMAIL, "password").convertToJSON().toString())
                )
                .andExpect(status().isOk())
                .andReturn();

        // Cookie
        Cookie cookie = mvcResult.getResponse().getCookie(COOKIE_NAME);

        // Fetch image
        this.MOCK_MVC
                .perform(get("/api/v1/account").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body").value("No profile photo"));
    }

    @Test
    @Order(2)
    void upload_profile_picture() throws Exception {
        // Login
        MvcResult mvcResult = this.MOCK_MVC
                .perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new AuthDTO(ADMIN_EMAIL, "password").convertToJSON().toString())
                )
                .andExpect(status().isOk())
                .andReturn();

        // Cookie
        Cookie cookie = mvcResult.getResponse().getCookie(COOKIE_NAME);

        // Upload image
        this.MOCK_MVC
                .perform(multipart("/api/v1/account/upload").file(new MockMultipartFile(
                        "file",
                        "image3.jpeg",
                        "image/jpeg",
                        "Test image upload".getBytes())).cookie(cookie)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body").value("Uploaded"));
    }

    @Test
    @Order(3)
    void password_reset() throws Exception {
        // Login
        MvcResult mvcResult = this.MOCK_MVC
                .perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new AuthDTO(ADMIN_EMAIL, "password").convertToJSON().toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.principal").value(ADMIN_EMAIL))
                .andReturn();

        // Cookie
        Cookie cookie = mvcResult.getResponse().getCookie(COOKIE_NAME);

        // Change password
        String new_password = "developer";
        this.MOCK_MVC
                .perform(put("/api/v1/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ResetDTO(new_password).convertToJSON().toString())
                        .cookie(cookie)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Password changed!"));

        // Validate old cookie has failed
        this.MOCK_MVC
                .perform(get("/test/admin").cookie(cookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Full authentication is required to access this resource")
                )
                .andExpect(jsonPath("$.httpStatus").value("UNAUTHORIZED"));

        // Validate new password works
        this.MOCK_MVC
                .perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new AuthDTO(ADMIN_EMAIL, new_password).convertToJSON().toString())
                )
                .andExpect(status().isOk());
    }

}