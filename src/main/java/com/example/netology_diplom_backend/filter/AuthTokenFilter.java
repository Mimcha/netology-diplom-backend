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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);
    public AuthTokenFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Оборачиваем запрос и ответ, чтобы можно было прочитать тело несколько раз
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            // Логируем заголовок auth-token
            String authToken = wrappedRequest.getHeader("auth-token");
            if (authToken != null) {
                logger.debug("Received auth-token header: {}", authToken);
            } else {
                logger.debug("No auth-token header found in the request");
            }

            // Логируем все заголовки
            Enumeration<String> headerNames = wrappedRequest.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    logger.debug("Header: {} = {}", headerName, wrappedRequest.getHeader(headerName));
                }
            }

            // Логируем параметры запроса
            Map<String, String[]> params = wrappedRequest.getParameterMap();
            if (!params.isEmpty()) {
                params.forEach((key, values) -> logger.debug("Parameter: {} = {}", key, Arrays.toString(values)));
            }

            // Логируем cookies
            Cookie[] cookies = wrappedRequest.getCookies();
            if (cookies != null && cookies.length > 0) {
                Arrays.stream(cookies).forEach(cookie ->
                        logger.debug("Cookie: {} = {}", cookie.getName(), cookie.getValue()));
            } else {
                logger.debug("No cookies found in the request");
            }

            // Выполняем следующий фильтр / обработку контроллером
            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } finally {
            // Логируем тело запроса
            logRequestBody(wrappedRequest);

            // Логируем тело ответа
            logResponseBody(wrappedResponse);

            // Копируем тело ответа обратно в оригинальный response
            wrappedResponse.copyBodyToResponse();
        }
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