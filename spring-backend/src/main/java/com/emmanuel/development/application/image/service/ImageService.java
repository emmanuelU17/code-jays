package com.emmanuel.development.application.image.service;

import com.emmanuel.development.application.image.entity.ImageEntity;
import com.emmanuel.development.application.image.repository.ImageRepository;
import com.emmanuel.development.application.image.response.ImageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpStatus.*;

@Service @Slf4j
public class ImageService {

    private final ImageRepository imageRepository;

    private final FilesUtils filesUtils;

    public ImageService(ImageRepository imageRepository, FilesUtils filesUtils) {
        this.imageRepository = imageRepository;
        this.filesUtils = filesUtils;
    }

    public ResponseEntity<?> upload_image (MultipartFile file) {
        Path uploadPath = Paths.get("uploads");

        if (!this.filesUtils.exists(uploadPath)) {
            try {
                this.filesUtils.create_directory(uploadPath);
            } catch (IOException ex) {
                return new ResponseEntity<>(
                        "Server error please try again " + ex.getMessage(), INTERNAL_SERVER_ERROR);
            }
        }

        ImageEntity imageEntity = new ImageEntity();
        try (InputStream inputStream = file.getInputStream()) {
            String name = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            this.filesUtils.save_to_folder(file, imageEntity, inputStream, name, uploadPath);
        } catch (IOException ex) {
            return new ResponseEntity<>("Error saving image " + ex.getMessage(), BAD_REQUEST);
        }

        this.imageRepository.save(imageEntity);
        return new ResponseEntity<>("Uploaded", OK);
    }

    /** Fetches all images with consideration of pagination */
    public List<ImageResponse> fetch_images (Integer page, Integer size) {
        return this.imageRepository
                .findAll(PageRequest.of(page, size)) //
                .stream()
                .map(imageEntity -> {
                    ImageResponse imageResponse = null;
                    Path path = Paths.get(imageEntity.getPath());

                    try {
                        imageResponse = new ImageResponse(
                                path.getFileName().toString(),
                                Files.probeContentType(path),
                                Files.readAllBytes(path)
                        );
                    } catch (IOException e) {
                        log.error("Error fetching images");
                    }

                    return imageResponse;
                }) //
                .toList();
    }

    /** Method returns the total images in our DB */
    public int fetch_total_elements() {
        return this.imageRepository.total();
    }

}
