package com.example.ola;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class OlaApplication {

	public static void main(String[] args) {
		SpringApplication.run(OlaApplication.class, args);
	}

}
