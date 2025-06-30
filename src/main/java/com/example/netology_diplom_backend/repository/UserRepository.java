package com.example.netology_diplom_backend.repository;

import com.example.netology_diplom_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByLogin(String login);
}
