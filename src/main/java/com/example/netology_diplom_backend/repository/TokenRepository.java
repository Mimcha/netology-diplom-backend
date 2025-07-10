package com.example.netology_diplom_backend.repository;

import com.example.netology_diplom_backend.model.Token;
import com.example.netology_diplom_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, String> {
    Optional<Token> findByTokenAndActiveTrue(String token);

    void deleteByToken(String token);

    void deleteAllByUser(User user);
}
