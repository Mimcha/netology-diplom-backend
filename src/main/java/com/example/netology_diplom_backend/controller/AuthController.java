package com.example.netology_diplom_backend.controller;

import com.example.netology_diplom_backend.dto.LoginRequest;
import com.example.netology_diplom_backend.dto.LoginResponse;
import com.example.netology_diplom_backend.service.AuthService;
import com.example.netology_diplom_backend.dto.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/login")
//@CrossOrigin (value = "http://localhost:8081",allowCredentials = "true")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.getLogin(), request.getPassword());
        if (token == null) {
            return ResponseEntity.status(400).body(new ErrorResponse("Bad credentials", 1));
        }
        return ResponseEntity.ok(new LoginResponse(token));
    }
}