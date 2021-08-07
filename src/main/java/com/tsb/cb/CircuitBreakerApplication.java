package com.tsb.cb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableAspectJAutoProxy
public class CircuitBreakerApplication {
	
	
	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}
	
	public static void main(String[] args) {
		
		SpringApplication.run(CircuitBreakerApplication.class, args);
		
		System.out.println("Hello this is starter application");
		
	}

}
