package com.example.netology_diplom_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Component
@ConfigurationProperties(prefix = "token")
@Data
public class ApplicationProperties {
    private int expiration;
}