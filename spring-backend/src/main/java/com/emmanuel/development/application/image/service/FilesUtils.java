package com.emmanuel.development.application.image.service;

import com.emmanuel.development.application.image.entity.ImageEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

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
            InputStream inputStream,
            String fileName,
            Path uploadPath
    ) throws IOException {
        Path filePath = uploadPath.resolve(fileName);
        imageEntity.setName(file.getOriginalFilename());
        imageEntity.setPath(filePath.toAbsolutePath().toString());
        imageEntity.setImage_type(Files.probeContentType(filePath.toAbsolutePath()));
        Files.copy(inputStream, filePath, REPLACE_EXISTING);
    }

    /** Uploads user image to folder */
    public String save_to_profile_folder(
            InputStream inputStream,
            Path uploadPath,
            String fileName
    ) throws IOException {
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(inputStream, filePath, REPLACE_EXISTING);
        return filePath.toAbsolutePath().toString();
    }

}
