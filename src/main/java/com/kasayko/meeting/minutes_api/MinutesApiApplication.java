package com.kasayko.meeting.minutes_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MinutesApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MinutesApiApplication.class, args);
	}

}
