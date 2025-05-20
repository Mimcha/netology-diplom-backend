package com.example.netology_diplom_backend.service;

import com.example.netology_diplom_backend.dto.FileResponse;
import com.example.netology_diplom_backend.model.FileMetadata;
import com.example.netology_diplom_backend.model.User;
import com.example.netology_diplom_backend.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.print.Pageable;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private final AuthService authService;
    private final Path storageLocation;
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    public FileService(AuthService authService, FileRepository fileRepository,
                       @Value("${storage.location}") String storagePath) {
        this.authService = authService;
        this.fileRepository = fileRepository;
        this.storageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(storageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create upload directory", ex);
        }
    }

    public void uploadFile(String token, String filename, MultipartFile file) throws IOException {
        User user = authService.getUserFromToken(token);
        String storedName = UUID.randomUUID() + "_" + filename;
        Path targetLocation = storageLocation.resolve(storedName);

        file.transferTo(targetLocation);

        FileMetadata metadata = new FileMetadata();
        metadata.setOriginalName(filename);
        metadata.setStoredName(storedName);
        metadata.setSize(file.getSize());
        metadata.setUser(user);

        fileRepository.save(metadata);
    }

    public void deleteFile(String token, String filename) {
        User user = authService.getUserFromToken(token);
        FileMetadata metadata = fileRepository.findByUserAndOriginalName(user, filename);

        if (metadata != null) {
            Path filePath = storageLocation.resolve(metadata.getStoredName());
            try {
                Files.delete(filePath);
                fileRepository.delete(metadata);
            } catch (IOException ex) {
                throw new RuntimeException("Error deleting file", ex);
            }
        }
    }

    public UrlResource downloadFile(String token, String filename) {
        User user = authService.getUserFromToken(token);
        FileMetadata metadata = fileRepository.findByUserAndOriginalName(user, filename);

        if (metadata == null) {
            throw new RuntimeException("File not found");
        }

        Path filePath = storageLocation.resolve(metadata.getStoredName());

        try {
            if (!Files.exists(filePath)) {
                throw new RuntimeException("File does not exist on disk: " + filePath);
            }
            if (!Files.isReadable(filePath)) {
                throw new RuntimeException("File is not readable: " + filePath);
            }

            URI uri = new URI("file", null,
                    URLEncoder.encode(filePath.toAbsolutePath().toString(), StandardCharsets.UTF_8),
                    null);
            return new UrlResource(uri);

        } catch (Exception e) {
            logger.error("Error loading file: {}", filename, e);
            throw new RuntimeException("Error reading file", e);
        }
    }

    public void renameFile(String token, String oldName, String newName) {
        User user = authService.getUserFromToken(token);
        FileMetadata metadata = fileRepository.findByUserAndOriginalName(user, oldName);

        if (metadata != null) {
            metadata.setOriginalName(newName);
            fileRepository.save(metadata);
        }
    }

    public List<FileResponse> getFileList(String token, int limit) {
        User user = authService.getUserFromToken(token);
        List<FileMetadata> files = fileRepository.findTopByUserOrderByOriginalNameAsc(user, (Pageable) PageRequest.of(0, limit));

        return files.stream()
                .map(f -> {
                    FileResponse response = new FileResponse();
                    response.setFilename(f.getOriginalName());
                    response.setSize(f.getSize());
                    return response;
                })
                .collect(Collectors.toList());
    }
}