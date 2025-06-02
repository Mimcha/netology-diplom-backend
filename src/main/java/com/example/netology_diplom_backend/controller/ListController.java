package com.example.netology_diplom_backend.controller;
import com.example.netology_diplom_backend.dto.FileResponse;
import com.example.netology_diplom_backend.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/list")
@RequiredArgsConstructor
public class ListController {
    private final FileService fileService;

    @GetMapping
    public ResponseEntity<List<FileResponse>> getFileList(
            @RequestHeader("auth-token") String token,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<FileResponse> files = fileService.getFileList(token, limit);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }
}