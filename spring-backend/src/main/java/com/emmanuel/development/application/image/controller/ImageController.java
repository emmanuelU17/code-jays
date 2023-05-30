package com.emmanuel.development.application.image.controller;

import com.emmanuel.development.application.image.service.ImageService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.HttpStatus.OK;

@RestController @RequestMapping(path = "/api/v1/image")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping
    public ResponseEntity<?> upload_image(@Param(value = "file")MultipartFile file) {
        return this.imageService.upload_image(file);
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> fetch_images (
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {
        return new ResponseEntity<>(this.imageService.fetch_images(page, size), OK);
    }

}
