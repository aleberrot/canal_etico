package com.pantteon.canal_etico.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {

    private final Path storagePath = Paths.get("uploads");

    public String guardarArchivo(MultipartFile file) {

        try {

            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }

            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            Path filePath = storagePath.resolve(filename);

            Files.copy(file.getInputStream(), filePath);

            return filename;

        } catch (IOException e) {
            throw new RuntimeException("Error guardando archivo");
        }
    }
}