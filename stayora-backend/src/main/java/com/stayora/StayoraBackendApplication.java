package com.stayora;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StayoraBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(StayoraBackendApplication.class, args);
	}

}
