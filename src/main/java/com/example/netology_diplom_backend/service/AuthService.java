package com.example.netology_diplom_backend.service;
import com.example.netology_diplom_backend.model.Token;
import com.example.netology_diplom_backend.model.User;
import com.example.netology_diplom_backend.repository.TokenRepository;
import com.example.netology_diplom_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = (Logger) LoggerFactory.getLogger(AuthService.class);

    public AuthService(UserRepository userRepository,
                       TokenRepository tokenRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User authenticate(String email, String rawPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();

        if (passwordEncoder.matches(rawPassword, user.getPassword())) {
            return user;
        } else {
            return null;
        }
    }

    public void logout(String token) {
        Optional<Token> tokenOpt = tokenRepository.findById(token);
        tokenOpt.ifPresent(t -> {
            t.setActive(false);
            tokenRepository.save(t);
        });
    }

    public boolean validateToken(String token) {
        return tokenRepository.findByTokenAndActiveTrue(token).isPresent();
    }

    public Authentication getAuthentication(String token) {
        Optional<Token> tokenOpt = tokenRepository.findByTokenAndActiveTrue(token);
        if (tokenOpt.isEmpty()) {
            logger.debug("Token not found or inactive: {}", token);
            return null;
        }
        User user = tokenOpt.get().getUser();

        List<GrantedAuthority> authorities = Collections.emptyList();

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        logger.debug("Created Authentication for user: {}", user.getEmail());
        return auth;
    }

    public User getUserByToken(String token) {
        return tokenRepository.findByTokenAndActiveTrue(token)
                .map(Token::getUser)
                .orElse(null);
    }

    /**
     * Создаёт и сохраняет новый токен для пользователя, возвращает строку токена
     */
    public String createTokenForUser(User user) {
        String tokenValue = UUID.randomUUID().toString();

        Token token = new Token();
        token.setToken(tokenValue);
        token.setUser(user);
        token.setActive(true);

        tokenRepository.save(token);

        return tokenValue;
    }
}