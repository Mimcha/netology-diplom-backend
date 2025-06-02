package com.example.netology_diplom_backend.service;

import com.example.netology_diplom_backend.dto.FileResponse;
import com.example.netology_diplom_backend.model.FileMetadata;
import com.example.netology_diplom_backend.model.User;
import com.example.netology_diplom_backend.repository.FileRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileService {

    private final AuthService authService;
    private final FileRepository fileRepository;
    private final Path storageLocation;

    @Autowired
    public FileService(AuthService authService, FileRepository fileRepository,
                       @Value("${storage.location}") String storagePath) {
        this.authService = authService;
        this.fileRepository = fileRepository;
        this.storageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
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
        metadata.setUser (user);

        fileRepository.save(metadata);
    }

    public void deleteFile(String token, String filename) {
        User user = authService.getUserFromToken(token);
        FileMetadata metadata = fileRepository.findByUserAndOriginalName(user, filename);

        if (metadata != null) {
            Path filePath = storageLocation.resolve(metadata.getStoredName());
            try {
                Files.deleteIfExists(filePath);
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

            return new UrlResource(filePath.toUri());

        } catch (MalformedURLException e) {
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
        Pageable pageable = PageRequest.of(0, limit);

        List<FileMetadata> files = fileRepository.findTopByUserOrderByOriginalNameAsc(user, pageable);

        return files.stream()
                .map(f -> new FileResponse(f.getOriginalName(), f.getSize()))
                .collect(Collectors.toList());
    }
}
