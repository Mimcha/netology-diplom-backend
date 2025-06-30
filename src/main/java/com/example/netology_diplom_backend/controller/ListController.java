package com.example.netology_diplom_backend.controller;
import com.example.netology_diplom_backend.dto.FileResponse;
import com.example.netology_diplom_backend.dto.ErrorResponse;
import com.example.netology_diplom_backend.model.User;
import com.example.netology_diplom_backend.service.AuthService;
import com.example.netology_diplom_backend.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidParameterException;
import java.util.List;


@RestController
@RequestMapping("/list")
//@CrossOrigin (value = "http://localhost:8081",allowCredentials = "true")
//@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ListController {
    private final FileService fileService;
    private final AuthService authService;
/*
    @GetMapping
    public ResponseEntity<List<FileResponse>> getFileList(
            @RequestHeader("auth-token") String token,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<FileResponse> files = fileService.getFileList(token, limit);
            return ResponseEntity.ok(files);
//        } catch (UnauthorizedException e) {
        //   return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}*/

    @GetMapping
    public ResponseEntity<?> getFileList(
            @RequestHeader("auth-token") String token,
            @RequestParam(value = "limit", required = false) Integer limit) {
//            @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit) {
        try {
            // Проверяем токен, кидаем исключение при невалидности
            User user = authService.getUserFromToken(token);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Unauthorized: invalid auth-token", 1));
            }
            // Получаем список файлов с ограничением limit
            List<FileResponse> files = fileService.getFileList(String.valueOf(user), limit);
            return ResponseEntity.ok(files);
        } catch (InvalidParameterException e) {
            // Ошибка в параметрах запроса
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Bad request: " + e.getMessage(),2));
       /* } catch (UnauthorizedException e) {
            // Ошибка авторизации
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Unauthorized: " + e.getMessage(),3));*/
        } catch (Exception e) {
            // Внутренняя ошибка сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error",3));
        }
    }
}