package com.example.netology_diplom_backend;

import com.example.netology_diplom_backend.model.User;
import com.example.netology_diplom_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByLogin("admin@example.com") == null) {
            User admin = new User();
            admin.setLogin("admin@example.com");
            admin.setPasswordHash(passwordEncoder.encode("password"));
            userRepository.save(admin);
        }
        if (userRepository.findByLogin("admin1@example.com") == null) {
            User admin = new User();
            admin.setLogin("admin1@example.com");
            admin.setPasswordHash(passwordEncoder.encode("password"));
            userRepository.save(admin);
        }
        if (userRepository.findByLogin("admin2@example.com") == null) {
            User admin = new User();
            admin.setLogin("admin2@example.com");
            admin.setPasswordHash(passwordEncoder.encode("password"));
            userRepository.save(admin);
        }
    }
}
