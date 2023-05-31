package com.emmanuel.development.application.image.controller;

import com.emmanuel.development.application.image.repository.ImageRepository;
import com.emmanuel.development.application.image.service.ImageService;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@TestPropertySource(locations = "classpath:application-dev.properties")
class ImageControllerTest {

    @Autowired private ImageService imageService;

    @Autowired private MockMvc MOCK_MVC;

    @Autowired private ImageRepository imageRepository;

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
        this.imageService.upload_image(new MockMultipartFile(
           "file", "image1.jpeg", "image/jpeg", "Test image upload".getBytes()
        ));
        this.imageService.upload_image(new MockMultipartFile(
                "file", "image3.jpeg", "image/jpeg", "Test image upload".getBytes()
        ));
        this.imageService.upload_image(new MockMultipartFile(
                "file", "image4.jpeg", "image/jpeg", "Test image upload".getBytes()
        ));
        this.imageService.upload_image(new MockMultipartFile(
                "file", "image5.jpeg", "image/jpeg", "Test image upload".getBytes()
        ));
        this.imageService.upload_image(new MockMultipartFile(
                "file", "image6.jpeg", "image/jpeg", "Test image upload".getBytes()
        ));
    }

    @AfterEach
    void tearDown() {
        this.imageRepository.deleteAll();
    }

    @Test
    void upload_image() throws Exception {
        this.MOCK_MVC
                .perform(multipart("/api/v1/image").file(new MockMultipartFile(
                        "file", "image2.jpeg",
                "image/jpeg", "Test image upload".getBytes()
                )))
                .andExpect(status().isOk())
                .andExpect(content().string("Uploaded"));
    }

    @Test
    void fetch_images() throws Exception {
        this.MOCK_MVC
                .perform(get("/api/v1/image")
                        .param("page", String.valueOf(0))
                        .param("page", String.valueOf(10))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void total_elements() throws Exception {
        this.MOCK_MVC
                .perform(get("/api/v1/image/total"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

}