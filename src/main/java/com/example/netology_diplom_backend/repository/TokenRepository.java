package com.example.netology_diplom_backend.repository;

import com.example.netology_diplom_backend.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, String> {
    Token findByToken(String token);

    Token findByUserId(Long userId);
}
