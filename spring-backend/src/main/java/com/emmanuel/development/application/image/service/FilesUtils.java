package com.emmanuel.development.application.image.service;

import com.emmanuel.development.application.image.entity.ImageEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class FilesUtils {

    /** Method is responsible for verifying folder to upload images exists */
    public boolean exists(Path path) {
        return Files.exists(path);
    }

    /** Method is responsible for creating a folder */
    public void create_directory(Path path) throws IOException {
        Files.createDirectories(path);
    }

    /** Method responsible for saving image to a folder */
    public void save_to_folder(
            MultipartFile file,
            ImageEntity imageEntity,
            String fileName,
            Path uploadPath
    ) throws IOException {
        InputStream inputStream = file.getInputStream();
        Path filePath = uploadPath.resolve(fileName);
        imageEntity.setName(file.getOriginalFilename());
        imageEntity.setPath(filePath.toAbsolutePath().toString());
        imageEntity.setImage_type(Files.probeContentType(filePath.toAbsolutePath()));
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
    }

}
