package com.example.netology_diplom_backend.controller;
import com.example.netology_diplom_backend.dto.LoginRequest;
import com.example.netology_diplom_backend.model.User;
import com.example.netology_diplom_backend.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getLogin();
        String password = loginRequest.getPassword();

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email и пароль должны быть заполнены"));
        }

        User user = authService.authenticate(email, password);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Неверный email или пароль"));
        }

        // Создаём токен для пользователя
        String token = authService.createTokenForUser(user);

        Map<String, String> responseBody = Map.of("auth-token", token);

        ResponseCookie cookie = ResponseCookie.from("auth-token", token)
                .httpOnly(true)
                .secure(false) // В production установите true
                .path("/")
                .maxAge(3600) // 1 час
                .sameSite("None")
                .build();

        return ResponseEntity.ok()
                .header("X-User-Email", user.getEmail())
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header("Access-Control-Expose-Headers", "X-User-Email")
                .body(responseBody);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("auth-token") String token) {
        authService.logout(token);
        return ResponseEntity.ok().build();
    }
}