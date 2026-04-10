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
        try {
            rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(rootLocation);
            log.info("📁 Upload directory ready: {}", rootLocation);
        } catch (IOException e) {
            throw new FileStorageException("Could not create upload directory: " + uploadDir, e);
        }
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Cannot store empty file");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        String storedFilename = UUID.randomUUID().toString() + "." + extension;

        try {
            // Resolve destination and normalize — toAbsolutePath() ensures Windows paths work correctly
            Path destination = rootLocation.resolve(storedFilename).toAbsolutePath().normalize();

            // Security check: destination must be inside rootLocation
            if (!destination.startsWith(rootLocation)) {
                throw new FileStorageException("Cannot store file outside upload directory: " + storedFilename);
            }

            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            log.info("📎 File stored: {}", storedFilename);
            return storedFilename;

        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + originalFilename, e);
        }
    }

    public Path load(String filename) {
        return rootLocation.resolve(filename).normalize();
    }

    public void delete(String filename) {
        try {
            Path filePath = rootLocation.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
            log.info("🗑 File deleted: {}", filename);
        } catch (IOException e) {
            log.warn("Could not delete file: {}", filename);
        }
    }

    public String getFileUrl(String storedFilename) {
        return "/uploads/" + storedFilename;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}