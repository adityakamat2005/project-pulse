package com.projectpulse.service;

import com.projectpulse.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        rootLocation = Paths.get(uploadDir).toAbsolutePath();
        try {
            Files.createDirectories(rootLocation);
            log.info("📁 Upload directory ready: {}", rootLocation);
        } catch (IOException e) {
            throw new FileStorageException("Could not create upload directory: " + uploadDir, e);
        }
    }

    /**
     * Store a file and return its stored file name (UUID-based).
     */
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("Cannot store empty file");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        String storedFilename = UUID.randomUUID().toString() + "." + extension;

        try {
            Path destination = rootLocation.resolve(storedFilename).normalize();
            // Security: ensure destination is within upload root
            if (!destination.startsWith(rootLocation)) {
                throw new FileStorageException("Cannot store file outside upload directory");
            }
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            log.info("📎 File stored: {}", storedFilename);
            return storedFilename;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + originalFilename, e);
        }
    }

    /**
     * Load a file as a Path for streaming/download.
     */
    public Path load(String filename) {
        return rootLocation.resolve(filename).normalize();
    }

    /**
     * Delete a stored file by name.
     */
    public void delete(String filename) {
        try {
            Path filePath = rootLocation.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
            log.info("🗑 File deleted: {}", filename);
        } catch (IOException e) {
            log.warn("Could not delete file: {}", filename);
        }
    }

    /**
     * Get public URL path for a stored file.
     */
    public String getFileUrl(String storedFilename) {
        return "/uploads/" + storedFilename;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
