package com.zingly.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
/** Bootstraps the Spring Boot event-processing application. */
public class EventsApplication {

	/** Starts the Spring Boot application. */
	public static void main(String[] args) {
		SpringApplication.run(EventsApplication.class, args);
	}

}
