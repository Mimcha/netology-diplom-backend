package com.example.netology_diplom_backend.controller;

import com.example.netology_diplom_backend.dto.RenameFileRequest;
import com.example.netology_diplom_backend.model.FileMetadata;
import com.example.netology_diplom_backend.model.User;
import com.example.netology_diplom_backend.service.AuthService;
import com.example.netology_diplom_backend.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private final AuthService authService;
    private final FileService fileService;

    public FileController(AuthService authService, FileService fileService) {
        this.authService = authService;
        this.fileService = fileService;
    }

    @PostMapping("/file")
    public ResponseEntity<?> uploadFile(
            @RequestHeader("auth-token") String token,
            @RequestParam("filename") String filename,
            @RequestParam("file") MultipartFile file) throws IOException {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        User user = authService.getUserByToken(token);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        fileService.uploadFile(user, file, filename);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/file")
    public ResponseEntity<?> deleteFile(
            @RequestHeader("auth-token") String token,
            @RequestParam("filename") String filename) throws IOException {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        User user = authService.getUserByToken(token);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        fileService.deleteFile(user, filename);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/file")
    public void downloadFile(
            @RequestHeader("auth-token") String token,
            @RequestParam("filename") String filename,
            HttpServletResponse response) throws IOException {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        User user = authService.getUserByToken(token);
        if (user == null) {
            response.setStatus(401);
            return;
        }

        try (InputStream is = fileService.getFile(user, filename)) {
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            is.transferTo(response.getOutputStream());
            response.flushBuffer();
        }
    }

    @PutMapping("/file")
    public ResponseEntity<?> renameFile(
            @RequestHeader("auth-token") String token,
            @RequestParam("filename") String oldName,
            @RequestBody RenameFileRequest request) throws IOException {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        User user = authService.getUserByToken(token);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        fileService.renameFile(user, oldName, request.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<?> listFiles(
            @RequestHeader("auth-token") String token,
            @RequestParam(value = "limit", required = false, defaultValue = "0") int limit) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        logger.debug("FileController: Token : {}", token);
        User user = authService.getUserByToken(token);
        logger.debug("FileController: User : {}",user);
        if (user == null) {
             return ResponseEntity.status(401).build();
        }

        List<FileMetadata> files = fileService.listFiles(user, limit);

        // Формируем простой JSON-список с метаданными
        var result = files.stream()
                .map(f -> Map.of(
                        "filename", f.getFilename(),
                        "size", f.getSize(),
                        "hash", f.getHash()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
