package com.bot.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HealthApplication {

	public static void main(String[] args) {
		SpringApplication.run(HealthApplication.class, args);
		System.out.println("Application Started ...");
	}

}
