package com.example.netology_diplom_backend.filter;

import com.example.netology_diplom_backend.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);
    public AuthTokenFilter(AuthService authService) {
        this.authService = authService;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Оборачиваем запрос для возможности многократного чтения тела
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            // Пропускаем preflight-запросы (OPTIONS)
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                response.setStatus(HttpServletResponse.SC_OK);
                return;
            }

            // Пропускаем запросы к /login без проверки токена
            String path = request.getRequestURI();
            if ("/login".equals(path)) {
                filterChain.doFilter(wrappedRequest, response);
                return;
            }

            // Извлекаем токен из заголовка или куки
            String token = extractToken(wrappedRequest);

            // Если токен найден и валиден, устанавливаем аутентификацию
            if (token != null && authService.validateToken(token)) {
                String username = authService.getUsernameFromToken(token);
                if (username != null) {
                    // Устанавливаем аутентификацию в контекст
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.emptyList()
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    logger.debug("Authentication set for user: {}", username);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing authentication", e);
        } finally {
            // Выполняем цепочку фильтров
            filterChain.doFilter(wrappedRequest, response);

            // Логируем тело запроса
            logRequestBody(wrappedRequest);

            // Логируем тело ответа
            logResponseBody(wrappedResponse);

            // Копируем тело ответа обратно в оригинальный response
            wrappedResponse.copyBodyToResponse();
        }
    }

    private String extractToken(HttpServletRequest request) {
        // 1. Проверяем заголовок "auth-token"
        String token = request.getHeader("auth-token");
        if (token != null && !token.isEmpty()) {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            return token;
        }

        // 2. Проверяем куки
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("auth-token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                    }
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private void logRequestBody(ContentCachingRequestWrapper request) {
        byte[] buf = request.getContentAsByteArray();
        if (buf.length > 0) {
            try {
                String payload = new String(buf, request.getCharacterEncoding());
                logger.debug("Request body: {}", payload);
            } catch (UnsupportedEncodingException e) {
                logger.warn("Could not decode request body", e);
            }
        } else {
            logger.debug("Request body is empty");
        }
    }

    private void logResponseBody(ContentCachingResponseWrapper response) {
        byte[] buf = response.getContentAsByteArray();
        if (buf.length > 0) {
            try {
                String payload = new String(buf, response.getCharacterEncoding());
                logger.debug("Response body: {}", payload);
            } catch (UnsupportedEncodingException e) {
                logger.warn("Could not decode response body", e);
            }
        } else {
            logger.debug("Response body is empty");
        }
    }

}