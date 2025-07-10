package com.example.netology_diplom_backend.service;

import com.example.netology_diplom_backend.model.FileMetadata;
import com.example.netology_diplom_backend.model.User;
import com.example.netology_diplom_backend.repository.FileMetadataRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {

    private final FileMetadataRepository fileMetadataRepository;

    private final Path rootPath;

    public FileService(FileMetadataRepository fileMetadataRepository,
                       @Value("${app.file-storage.path}") String storagePath) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.rootPath = Paths.get(storagePath);
        try {
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory", e);
        }
    }

    /**
     * Загрузить файл
     */
    public void uploadFile(User user, MultipartFile file, String filename) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Проверка на существование файла с таким именем у пользователя
        Optional<FileMetadata> existing = fileMetadataRepository.findByFilenameAndOwner(filename, user);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("File with this name already exists");
        }

        // Сохраняем файл на диск
        Path userDir = rootPath.resolve(user.getEmail());
        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
        }

        Path filePath = userDir.resolve(filename);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Вычисляем хэш файла (SHA-256)
        String hash;
        try (InputStream in = Files.newInputStream(filePath)) {
            hash = DigestUtils.sha256Hex(in);
        }

        // Сохраняем метаданные в БД
        FileMetadata meta = new FileMetadata(filename, file.getSize(), hash, user);
        fileMetadataRepository.save(meta);
    }

    /**
     * Удалить файл
     */
    public void deleteFile(User user, String filename) throws IOException {
        Optional<FileMetadata> metaOpt = fileMetadataRepository.findByFilenameAndOwner(filename, user);
        if (metaOpt.isEmpty()) {
            throw new FileNotFoundException("File not found");
        }

        Path filePath = rootPath.resolve(user.getEmail()).resolve(filename);
        Files.deleteIfExists(filePath);

        fileMetadataRepository.delete(metaOpt.get());
    }

    /**
     * Получить файл как ресурс (InputStream)
     */
    public InputStream getFile(User user, String filename) throws IOException {
        Optional<FileMetadata> metaOpt = fileMetadataRepository.findByFilenameAndOwner(filename, user);
        if (metaOpt.isEmpty()) {
            throw new FileNotFoundException("File not found");
        }

        Path filePath = rootPath.resolve(user.getEmail()).resolve(filename);
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File not found on disk");
        }

        return Files.newInputStream(filePath);
    }

    /**
     * Переименовать файл
     */
    public void renameFile(User user, String oldName, String newName) throws IOException {
        Optional<FileMetadata> metaOpt = fileMetadataRepository.findByFilenameAndOwner(oldName, user);
        if (metaOpt.isEmpty()) {
            throw new FileNotFoundException("File not found");
        }
        if (fileMetadataRepository.findByFilenameAndOwner(newName, user).isPresent()) {
            throw new IllegalArgumentException("File with new name already exists");
        }

        Path userDir = rootPath.resolve(user.getEmail());
        Path oldFile = userDir.resolve(oldName);
        Path newFile = userDir.resolve(newName);

        Files.move(oldFile, newFile, StandardCopyOption.REPLACE_EXISTING);

        FileMetadata meta = metaOpt.get();
        meta.setFilename(newName);
        fileMetadataRepository.save(meta);
    }

    /**
     * Получить список файлов пользователя
     */
    public List<FileMetadata> listFiles(User user, int limit) {
        List<FileMetadata> files = fileMetadataRepository.findAllByOwner(user);
        if (limit > 0 && files.size() > limit) {
            return files.subList(0, limit);
        }
        return files;
    }
}