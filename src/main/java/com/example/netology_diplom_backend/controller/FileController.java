package com.example.netology_diplom_backend.controller;

import com.example.netology_diplom_backend.service.FileService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import com.example.netology_diplom_backend.dto.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping
    public ResponseEntity<?> uploadFile(
            @RequestHeader("auth-token") String token,
            @RequestParam("filename") String filename,
            @RequestParam("file") MultipartFile file) {
        try {
            fileService.uploadFile(token, filename, file);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new ErrorResponse("Error uploading file", 2));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteFile(
            @RequestHeader("auth-token") String token,
            @RequestParam("filename") String filename) {
        try {
            fileService.deleteFile(token, filename);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new ErrorResponse("Error deleting file", 3));
        }
    }

    @GetMapping
    public ResponseEntity<Resource> downloadFile(
            @RequestHeader("auth-token") String token,
            @RequestParam("filename") String filename) {
        try {
            Resource file = (Resource) fileService.downloadFile(token, filename);
            if (file == null) {
                return ResponseEntity.status(404).build(); // Файл не найден
            }

            // Получаем имя файла из FileMetadata или из Resource
            String fileName = filename; // Или используйте другой способ получения имени файла

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"")
                    .body(file);
        } catch (Exception e) {
            return ResponseEntity.status(500).build(); // Внутренняя ошибка
        }
    }

    @PutMapping
    public ResponseEntity<?> renameFile(
            @RequestHeader("auth-token") String token,
            @RequestParam("filename") String oldName,
            @RequestBody Map<String, String> request) {
        String newName = request.get("name");
        try {
            fileService.renameFile(token, oldName, newName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new ErrorResponse("Error renaming file", 4));
        }
    }
}
