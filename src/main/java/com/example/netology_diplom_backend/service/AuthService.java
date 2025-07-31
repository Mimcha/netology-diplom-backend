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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

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
        }
        return null;
    }

    public void logout(String token) {
        tokenRepository.findById(token).ifPresent(tokenRepository::delete);
    }

    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        Optional<Token> tokenOpt = tokenRepository.findById(token);
        if (tokenOpt.isEmpty()) {
            return false;
        }

      /*  Token tokenEntity = tokenOpt.get();
        if (tokenEntity.getExpiration().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(tokenEntity);
            return false;
        }
*/
        return true;
    }

    public Authentication getAuthentication(String token) {
        Optional<Token> tokenOpt = tokenRepository.findById(token);
        if (tokenOpt.isEmpty()) {
            logger.debug("Token not found: {}", token);
            return null;
        }

        Token tokenEntity = tokenOpt.get();
        /*
        if (tokenEntity.getExpiration().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(tokenEntity);
            logger.debug("Token expired: {}", token);
            return null;
        }*/

        User user = tokenEntity.getUser();
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")
        );
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
        Optional<Token> tokenOpt = tokenRepository.findById(token);
        return tokenOpt.map(Token::getUser).orElse(null);
    }

    public String createTokenForUser(User user) {
        // Удаляем старый токен, если он существует
      //  tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiration = LocalDateTime.now().plusSeconds(3600); // 1 час

        Token token = new Token();
        token.setToken(tokenValue);
        token.setUser(user);
        //token.setExpiration(expiration);

        tokenRepository.save(token);
        return tokenValue;
    }

    /**
     * Извлекает email пользователя из токена
     * @param token строковое значение токена
     * @return email пользователя или null, если токен не найден или просрочен
     */
    public String getUsernameFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        Optional<Token> tokenOpt = tokenRepository.findById(token);
        if (tokenOpt.isEmpty()) {
            logger.debug("Token not found: {}", token);
            return null;
        }

        Token tokenEntity = tokenOpt.get();
      /*  if (tokenEntity.getExpiration().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(tokenEntity);
            logger.debug("Token expired: {}", token);
            return null;
        }
*/
        return tokenEntity.getUser().getEmail();
    }
}