package com.emmanuel.development.application.image.service;

import com.emmanuel.development.application.image.entity.ImageEntity;
import com.emmanuel.development.application.image.repository.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ActiveProfiles("dev")
@TestPropertySource(locations = "classpath:application-dev.properties")
class ImageServiceTest {

    private ImageService imageService;

    @Mock private ImageRepository imageRepository;

    @Mock private FilesUtils filesUtils;

    @BeforeEach
    void setUp() {
        this.imageService = new ImageService(this.imageRepository, filesUtils);
    }

    @Test
    void upload_image() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image1.jpeg",
                "image/jpeg",
                "Test image content".getBytes()
        );
        var image = new ImageEntity();
        image.setName("image1.jpeg");
        image.setImage_type("image/jpeg");
        image.setPath("uploads/image1.jpeg");

        // When
        when(this.filesUtils.exists(any(Path.class))).thenReturn(true);
        when(this.imageRepository.save(any(ImageEntity.class))).thenReturn(image);

        // Then
        var response = this.imageService.upload_image(file);
        assertEquals("Uploaded", response.getBody());
        assertEquals(OK, response.getStatusCode());
        verify(this.filesUtils, times(1)).save_to_folder(
                any(MockMultipartFile.class),
                any(ImageEntity.class),
                any(InputStream.class),
                anyString(),
                any(Path.class)
        );
        verify(this.imageRepository, times(1)).save(any(ImageEntity.class));
    }

    @Test
    void error_creating_folder_to_save_image() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "uploads/image1.jpeg",
                "image/jpeg",
                "Test image content".getBytes()
        );
        var image = new ImageEntity();
        image.setName("image1.jpeg");
        image.setImage_type("image/jpeg");
        image.setPath("uploads/image1.jpeg");

        // When
        when(this.filesUtils.exists(any(Path.class))).thenReturn(false);
        doThrow(IOException.class).when(this.filesUtils).create_directory(any(Path.class));

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, this.imageService.upload_image(file).getStatusCode());
    }

    @Test
    void error_saving_image() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "uploads/image1.jpeg",
                "image/jpeg",
                "Test image content".getBytes()
        );
        var image = new ImageEntity();
        image.setName("image1.jpeg");
        image.setImage_type("image/jpeg");
        image.setPath("uploads/image1.jpeg");

        // When
        when(this.filesUtils.exists(any(Path.class))).thenReturn(true);
        doThrow(IOException.class).when(this.filesUtils).save_to_folder(
                any(MockMultipartFile.class),
                any(ImageEntity.class),
                any(InputStream.class),
                anyString(),
                any(Path.class)
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, this.imageService.upload_image(file).getStatusCode());
    }

    @Test
    void fetch_images() {
        // Given/When
        when(imageRepository.findAll(PageRequest.of(0, 10))).thenReturn(page());

        // Then
        assertEquals(imageService.fetch_images(0, 10).size(), page().getContent().size());
    }

    @Test
    void total_elements() {
        // Given
        ImageEntity image1 = new ImageEntity();
        image1.setId(UUID.randomUUID());
        image1.setName("image1.jpg");
        image1.setPath("uploads/image1.jpg");
        image1.setImage_type("image/jpeg");

        ImageEntity image2 = new ImageEntity();
        image2.setId(UUID.randomUUID());
        image2.setName("image2.jpg");
        image2.setPath("uploads/image2.jpg");
        image1.setImage_type("image/jpeg");

        ImageEntity image3 = new ImageEntity();
        image1.setId(UUID.randomUUID());
        image1.setName("image3.jpg");
        image1.setPath("uploads/image3.jpg");
        image1.setImage_type("image/jpeg");

        ImageEntity image4 = new ImageEntity();
        image2.setId(UUID.randomUUID());
        image2.setName("image4.jpg");
        image2.setPath("uploads/image4.jpg");
        image1.setImage_type("image/jpeg");

        // When
        when(this.imageRepository.total()).thenReturn(4);

        // Then
        assertEquals(4, this.imageService.fetch_total_elements());
        verify(this.imageRepository, times(1)).total();
    }

    private Page<ImageEntity> page() {
        ImageEntity image1 = new ImageEntity();
        image1.setId(UUID.randomUUID());
        image1.setName("image1.jpg");
        image1.setPath("uploads/image1.jpg");
        image1.setImage_type("image/jpeg");

        ImageEntity image2 = new ImageEntity();
        image2.setId(UUID.randomUUID());
        image2.setName("image2.jpg");
        image2.setPath("uploads/image2.jpg");
        image1.setImage_type("image/jpeg");

        var list = List.of(image1, image2);

        return new PageImpl<>(list, PageRequest.of(0, 10), list.size());
    }

}