package com.example.netology_diplom_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.netology_diplom_backend", "com.example.netology_diplom_backend.config"})
public class NetologyDiplomBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(NetologyDiplomBackendApplication.class, args);
	}

}
