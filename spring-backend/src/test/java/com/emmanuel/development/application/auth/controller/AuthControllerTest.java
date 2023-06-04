package com.emmanuel.development.application.auth.controller;

import com.emmanuel.development.application.auth.dto.AuthDTO;
import com.emmanuel.development.application.auth.repository.AppUserRepository;
import com.emmanuel.development.application.auth.repository.RolesRepository;
import com.emmanuel.development.application.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@TestPropertySource(locations = "classpath:application-dev.properties")
class AuthControllerTest {

    @Value(value = "${admin.email}")
    private String ADMIN_EMAIL;

    @Value("${custom.max-session}")
    private int MAX_SESSION;

    @Value(value = "${custom.cookie.name}")
    private String COOKIE_NAME;

    @Autowired private MockMvc MOCK_MVC;

    @Autowired private RolesRepository roleRepository;

    @Autowired private AppUserRepository appUserRepository;

    @Autowired private AuthService authService;

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
        this.roleRepository.deleteAll();
        this.appUserRepository.deleteAll();
    }

    @Test
    @Order(1)
    void login() throws Exception {
        MvcResult mvcResult = this.MOCK_MVC
                .perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new AuthDTO(ADMIN_EMAIL, "password").convertToJSON().toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.principal").value(ADMIN_EMAIL))
                .andReturn();

        Cookie cookie = mvcResult.getResponse().getCookie(COOKIE_NAME);
        this.MOCK_MVC
                .perform(get("/test/admin").cookie(cookie))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    void register() throws Exception {
        // Role Admin can only create a new user
        this.MOCK_MVC
                .perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new AuthDTO("test@test.com", "password").convertToJSON().toString())
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().string("Registered"));
    }

    @Test
    @Order(3)
    void register_with_existing_credential() throws Exception {
        // Register
        this.MOCK_MVC
                .perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new AuthDTO(ADMIN_EMAIL, "password").convertToJSON().toString())
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ADMIN_EMAIL + " exists"))
                .andExpect(jsonPath("$.httpStatus").value("CONFLICT"));
    }

    @Test
    @Order(4)
    void logout() throws Exception {
        // Login
        MvcResult login = this.MOCK_MVC
                .perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new AuthDTO(ADMIN_EMAIL, "password").convertToJSON().toString())
                )
                .andExpect(status().isOk())
                .andReturn();
        Cookie cookie = login.getResponse().getCookie(COOKIE_NAME);

        // Logout
        this.MOCK_MVC
                .perform(get("/api/v1/auth/logout").cookie(cookie))
                .andExpect(status().isOk());

        // Verify cookie is invalid
        this.MOCK_MVC
                .perform(get("/test/admin").cookie(cookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Full authentication is required to access this resource")
                )
                .andExpect(jsonPath("$.httpStatus").value("UNAUTHORIZED"));
    }

    /** Test validating max session is 1 */
    @Test
    @Order(5)
    void validate_max_session() throws Exception {
        if (MAX_SESSION != 1) {
            return;
        }

        // Browser 1
        MvcResult login_one = this.MOCK_MVC
                .perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new AuthDTO(ADMIN_EMAIL, "password").convertToJSON().toString())
                )
                .andExpect(status().isOk())
                .andReturn();

        Cookie cookie_one = login_one.getResponse().getCookie(COOKIE_NAME);

        // Browser 2
        MvcResult login_two = this.MOCK_MVC
                .perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new AuthDTO(ADMIN_EMAIL, "password").convertToJSON().toString())
                )
                .andExpect(status().isOk())
                .andReturn();

        Cookie cookie_two = login_two.getResponse().getCookie(COOKIE_NAME);

        // Private route
        // Should return 401
        this.MOCK_MVC
                .perform(get("/test/admin").cookie(cookie_one))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Full authentication is required to access this resource")
                )
                .andExpect(jsonPath("$.httpStatus").value("UNAUTHORIZED"));

        // Should return 200
        this.MOCK_MVC
                .perform(get("/test/user").cookie(cookie_two))
                .andExpect(status().isOk());
    }

}