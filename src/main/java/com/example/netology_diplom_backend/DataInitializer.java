package com.example.netology_diplom_backend;
import com.example.netology_diplom_backend.model.User;
import com.example.netology_diplom_backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("user@example.com").isEmpty()) {
            User user = new User();
            user.setEmail("user@example.com");
            user.setPassword(passwordEncoder.encode("password123")); // хешируем пароль
            userRepository.save(user);
            System.out.println("User created: user@example.com / password123");
        }
    }
}
