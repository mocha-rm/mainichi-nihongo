package com.jhlab.mainichi_nihongo;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.TimeZone;

@SpringBootApplication
public class MainichiNihongoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MainichiNihongoApplication.class, args);
	}

	@PostConstruct
	public void init() {
		System.out.println("JVM TimeZone: " + TimeZone.getDefault().getID());
		System.out.println("createdAt: " + LocalDateTime.now());
	}
}
