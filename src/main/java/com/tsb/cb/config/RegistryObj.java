package com.tsb.cb.config;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

public class RegistryObj {
	
	private CircuitBreakerRegistry registry = null;
	
	private RegistryObj() {
		// TODO Auto-generated constructor stub
	}
	
	public CircuitBreakerRegistry getCircuitBreakerRegitry() {
		
		if(registry==null) {
			
			CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
				    .failureRateThreshold(50)
				    .waitDurationInOpenState(Duration.ofMillis(1000))
				    .permittedNumberOfCallsInHalfOpenState(2)
				    .slidingWindowSize(2)
				    .recordExceptions(IOException.class, TimeoutException.class)
				    .build();

				// Create a CircuitBreakerRegistry with a custom global configuration
			CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(circuitBreakerConfig);
			
		}
		
		return registry;
	}
	
	

}
