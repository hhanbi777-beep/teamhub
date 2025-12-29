package com.teamhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TeamhubApplication {

	public static void main(String[] args) {
		SpringApplication.run(TeamhubApplication.class, args);
	}

}
