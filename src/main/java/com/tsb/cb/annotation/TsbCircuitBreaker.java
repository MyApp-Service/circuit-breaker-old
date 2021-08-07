package com.tsb.cb.annotation;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import org.springframework.stereotype.Component;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

@Component
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TsbCircuitBreaker {
	
	String name() default "tsb-breaker";
	String fallback() default "fallback";
	boolean bulkhead() default false;
	boolean retry() default false;
	
	CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
		    .failureRateThreshold(50)
		    .waitDurationInOpenState(Duration.ofMillis(1000))
		    .permittedNumberOfCallsInHalfOpenState(10)
		    .slidingWindowSize(2)
		    .recordExceptions(IOException.class, TimeoutException.class)
		    .build();

		// Create a CircuitBreakerRegistry with a custom global configuration
		CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(circuitBreakerConfig);
		
		CircuitBreaker cb = registry.circuitBreaker("");
		
		/*
		 * // Create a custom configuration for a Bulkhead BulkheadConfig config =
		 * BulkheadConfig.custom() .maxConcurrentCalls(10)
		 * .maxWaitDuration(Duration.ofMillis(1)) .build();
		 * 
		 * // Create a BulkheadRegistry with a custom global configuration
		 * BulkheadRegistry bulkheadRegistry = BulkheadRegistry.of(config);
		 */
		
		
		
		

}
