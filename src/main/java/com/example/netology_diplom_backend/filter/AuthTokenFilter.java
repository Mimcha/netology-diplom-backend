package com.example.netology_diplom_backend.filter;

import com.example.netology_diplom_backend.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private AuthService authService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    public AuthTokenFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Логирование запроса (пример с исправленным printf)
        System.out.printf("Request method: %s, URL: %s%n", request.getMethod(), request.getRequestURL());

        // Получение токена из заголовка (с учётом регистра)
        String token = request.getHeader("Auth-Token");
        if (token == null) {
            token = request.getHeader("auth-token");
        }

        // Если токен не найден в заголовках — ищем в куках
        if (token == null || token.isBlank()) {
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("auth-token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        System.out.printf("Token found in cookie 'auth-token': %s%n", token);
                        break;
                    }
                }
            }
        }
        // Если токен начинается с "Bearer ", убираем префикс
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // Логируем итоговый токен
        System.out.printf("Using token: %s%n", token);

        // Проверка токена и установка Authentication в контекст
        if (token != null && !token.isBlank()) {
            Authentication auth = authService.getAuthentication(token);
            if (auth != null) {
                SecurityContextHolder.getContext().setAuthentication(auth);
                logger.debug("Authentication set for user: {}", auth.getName());
            } else {
                logger.debug("Authentication is null for token");
            }
        } else {
            logger.debug("No token found");
        }

        filterChain.doFilter(request, response);
    }
}
