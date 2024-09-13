package com.indium.backend_assignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class BackendAssignmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendAssignmentApplication.class, args);
	}

}
