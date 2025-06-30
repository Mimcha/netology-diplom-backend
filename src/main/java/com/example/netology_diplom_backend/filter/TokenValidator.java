package com.example.netology_diplom_backend.filter;

import com.example.netology_diplom_backend.model.Token;
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

    public String getUsernameFromToken(String token) {
        Token tokenEntity = tokenRepository.findByToken(token);
        if (tokenEntity == null) {
            return null; // или выбросить исключение
        }
        return tokenEntity.getUser().getLogin(); // возвращаем login, а не username
    }
}
