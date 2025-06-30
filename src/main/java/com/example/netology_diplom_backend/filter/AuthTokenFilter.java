package com.example.netology_diplom_backend.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {
    private final TokenValidator tokenValidator;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        // Пропустите preflight-запросы (OPTIONS)
        if ("OPTIONS".equals(request.getMethod())) {
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:8081");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type, auth-token");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        if ("/login".equals(path) || path.equals("/list")){
            // ✅ Правильно: вызываем doFilter
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader("auth-token");
        if (token == null || !tokenValidator.validateToken(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        }

        String username = tokenValidator.getUsernameFromToken(token);
// Можно передать пустой список или с одной ролью "ROLE_USER"
        List<SimpleGrantedAuthority> authorities = List.of(); // пустой список

// Если Spring Security ругается на отсутствие авторитетов, используйте:
// List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
        // ✅ Правильно: вызываем doFilter
        filterChain.doFilter(request, response);
    }
}
