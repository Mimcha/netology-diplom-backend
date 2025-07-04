package com.example.netology_diplom_backend.service;

import com.example.netology_diplom_backend.model.Token;
import com.example.netology_diplom_backend.model.User;
import com.example.netology_diplom_backend.repository.TokenRepository;
import com.example.netology_diplom_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${token.expiration}")
    private int expirationTime;

    public String login(String login, String password) {
        User user = userRepository.findByLogin(login);
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            return null;
        }

        Token existingToken = tokenRepository.findByUserId(user.getId());

        String newTokenValue = generateUniqueToken();
        LocalDateTime expiration = LocalDateTime.now().plusSeconds(expirationTime);

        if (existingToken != null) {
            // НЕ меняем ID
            existingToken.setToken(newTokenValue);
            existingToken.setExpiration(expiration);
            tokenRepository.save(existingToken);
        } else {
            Token tokenEntity = new Token();
            tokenEntity.setToken(newTokenValue);
            tokenEntity.setUser(user);
            tokenEntity.setExpiration(expiration);
            tokenRepository.save(tokenEntity);
        }

        return newTokenValue;
    }

    public boolean validateToken(String token) {
        if (token == null) return false;

        Token tokenEntity = tokenRepository.findByToken(token);
        if (tokenEntity == null) return false;

        if (tokenEntity.getExpiration().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(tokenEntity);
            return false;
        }

        return true;
    }

    public void logout(String token) {
        Token tokenEntity = tokenRepository.findByToken(token);
        if (tokenEntity != null) {
            tokenRepository.delete(tokenEntity);
        }
    }

    public User getUserFromToken(String token) {
        if (!validateToken(token)) return null;
        Token tokenEntity = tokenRepository.findByToken(token);
        return tokenEntity != null ? tokenEntity.getUser() : null;
    }

    private String generateUniqueToken() {
        String token;
        do {
            token = UUID.randomUUID().toString();
        } while (tokenRepository.findByToken(token) != null);
        return token;
    }
}
