package com.example.netology_diplom_backend.filter;

import com.example.netology_diplom_backend.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenValidator {
    private final TokenRepository tokenRepository;

    public boolean validateToken(String token) {
        return token != null && tokenRepository.findByToken(token) != null;
    }
}
