package com.project.blog.service;

import com.project.blog.config.UploadProperties;
import com.project.blog.exception.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    private static final Map<String, String> EXTENSION_BY_CONTENT_TYPE = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final UploadProperties properties;

    public StorageService(UploadProperties properties) {
        this.properties = properties;
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("profile.image.empty", "No file was provided.");
        }
        String contentType = file.getContentType();
        String extension = EXTENSION_BY_CONTENT_TYPE.get(contentType);
        if (extension == null) {
            throw new StorageException("profile.image.unsupported", "Unsupported file type: " + contentType);
        }

        String storedName = UUID.randomUUID() + "." + extension;
        Path target = Paths.get(properties.dir()).resolve(storedName);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target);
        } catch (IOException e) {
            log.error("Storage write failed for content type {}", contentType, e);
            throw new StorageException("profile.image.failed", "Could not store the file.", e);
        }
        return storedName;
    }
}
