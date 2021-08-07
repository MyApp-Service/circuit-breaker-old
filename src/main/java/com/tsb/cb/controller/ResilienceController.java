package com.tsb.cb.controller;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.tsb.cb.annotation.CircuitBreakerTSB;
import com.tsb.cb.annotation.TsbCircuitBreaker;
import com.tsb.cb.config.ResilienceConfig;
import com.tsb.cb.service.OpenAMClient;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

@RestController
@RequestMapping("api")
public class ResilienceController {
	
	@Autowired
	ResilienceConfig config;
	
	@Autowired
	OpenAMClient openAMClient;
	
	@Autowired
	RestTemplate template;
	
	/* @TsbCircuitBreaker(name = "ACB", fallback = "fallbackMethod") */
	@RequestMapping("first")
	public String firstMethod() {
		
		return "Hi First Method...";
	}
	
	@CircuitBreakerTSB(name = "backendA", fallback = "sescondMethodFallback")
	@RequestMapping("second")
	public String secondMethod() {
		
		/*config.processConfig("Hello");
		
		Class cls = this.getClass();
		
		TsbCircuitBreaker tsbcb = (TsbCircuitBreaker) cls.getAnnotation(TsbCircuitBreaker.class);
		
		CircuitBreaker cb = tsbcb.registry.circuitBreaker("Sample");
		
		Supplier<String> supplier = CircuitBreaker.decorateSupplier(cb, ()-> new RestTemplate().getForObject("http://localhost:/", null));
		
		String result = Try.ofSupplier(supplier)
			    .recover(throwable -> "Hello from Recovery").get();*/
		
		
		String response = template.getForObject("http://localhost:8081/info", String.class);
		
		/*
		 * if(true) { throw new RuntimeException("This has Error Please try again...");
		 * }
		 */
		 
		 
		
		return "Hi Second Method..."+response;
	}
	
	
	/* @TsbCircuitBreaker(name="enrolledLogin", fallback = "verifyUserFallback") */
	@RequestMapping("login")
	public ResponseEntity<String> enrolledLogin() {
		
		Class cls = this.getClass();
		
		TsbCircuitBreaker tsbcb = (TsbCircuitBreaker) cls.getAnnotation(TsbCircuitBreaker.class);
		
		Function<Integer, Integer> decoratedFunction = CircuitBreaker.decorateFunction(tsbcb.cb, openAMClient::verifyUser);
	        for (int i = 0; i < 10; i++) {
	            try {
	                if(i==5)
	                    Thread.sleep(3000);
	                decoratedFunction.apply(i);
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	        
	     return ResponseEntity.ok().body("I am rescued from failure");
	}
	
	public String verifyUserFallback() {
		
		return "Success";
	}
	
	public String serviceFallback() {
		
		return "Success";
	}
	
	public String sescondMethodFallback(Throwable t) {
		return "sescondMethodFallback";
	}

}
