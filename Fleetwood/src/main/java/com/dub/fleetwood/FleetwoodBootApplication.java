package com.dub.fleetwood;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


@SpringBootApplication
public class FleetwoodBootApplication extends WebMvcConfigurerAdapter {
	
	private static final Logger logger = LoggerFactory.getLogger(FleetwoodBootApplication.class);	
	
	public static void main(String[] args) {

		SpringApplication.run(FleetwoodBootApplication.class, args);
		logger.debug("--Application started--");
	}
	
}
