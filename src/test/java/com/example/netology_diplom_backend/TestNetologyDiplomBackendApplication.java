package com.example.netology_diplom_backend;

import org.springframework.boot.SpringApplication;

public class TestNetologyDiplomBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(NetologyDiplomBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
