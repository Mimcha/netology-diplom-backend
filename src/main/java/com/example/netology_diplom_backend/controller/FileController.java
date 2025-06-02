package com.example.netology_diplom_backend.controller;

import com.example.netology_diplom_backend.dto.ErrorResponse;
import com.example.netology_diplom_backend.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid input: " + e.getMessage(), 2));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Error uploading file: " + e.getMessage(), 2));
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
            return ResponseEntity.status(500).body(new ErrorResponse("Error deleting file: " + e.getMessage(), 3));
        }
    }

    @GetMapping
    public ResponseEntity<Resource> downloadFile(
            @RequestHeader("auth-token") String token,
            @RequestParam("filename") String filename) {
        try {
            Resource file = fileService.downloadFile(token, filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping
    public ResponseEntity<?> renameFile(
            @RequestHeader("auth-token") String token,
            @RequestParam("filename") String oldName,
            @RequestBody Map<String, String> request) {
        String newName = request.get("name");
        try {
            if (newName == null || newName.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("New name must not be empty", 4));
            }
            fileService.renameFile(token, oldName, newName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Error renaming file: " + e.getMessage(), 4));
        }
    }
}
