package com.tsb.cb.process;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import com.tsb.cb.annotation.CircuitBreakerTSB;
import com.tsb.cb.annotation.TsbCircuitBreaker;
import com.tsb.cb.config.ResilienceConfig;
import com.tsb.cb.controller.ResilienceController;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;

@Aspect
 @Component 
public class ResiliencyService<T> {
	
	@Around("@annotation(com.tsb.cb.annotation.TsbCircuitBreaker)") 
	public Object proceedResiliency(ProceedingJoinPoint joinPoint) throws Throwable {
		
		Class<T> cls = (Class<T>) joinPoint.getTarget().getClass();
		
		Method m = cls.getMethod(joinPoint.getSignature().getName());
		
		TsbCircuitBreaker tsbcb = m.getAnnotation(TsbCircuitBreaker.class);
		
		CircuitBreaker cb = tsbcb.registry.circuitBreaker(tsbcb.name());
		
		/*Supplier<Object> supplier = CircuitBreaker.decorateSupplier(cb, ()->{
				Object obj = null;
				try {
					
					obj = m.invoke(cls.newInstance(), m.getParameters());
					System.out.println("OBJ Val:---"+obj);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
					// TODO Auto-generated catch block
					System.out.println("Exception Msg:--"+ e.getMessage());
					System.out.println(e.getCause());
					e.printStackTrace();
				}
				return obj;
			
		});
		
		
		
		//joinPoint.proceed();
		
		Object result = Try.ofSupplier(supplier)
			    .recover(throwable -> {
			    	Object val = null;
					try {
						 val = cls.getMethod(tsbcb.fallback()).invoke(cls.newInstance(), null);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
							| NoSuchMethodException | SecurityException | InstantiationException e) {
						// TODO Auto-generated catch block
						System.out.println("Exception Msg fallback:--"+ e.getMessage());
						e.printStackTrace();
					}
					return val;
				}).get();*/
		//joinPoint.proceed();
		Thread.sleep(3000);
		CheckedFunction0 checkedSupplier =
				  CircuitBreaker.decorateCheckedSupplier(cb, () -> {
				   return joinPoint.proceed();
				});
				Try result = Try.of(checkedSupplier)
				        .recover(throwable -> {
				        	Object val = null;
							try {
								 val = cls.getMethod(tsbcb.fallback()).invoke(cls.newInstance(), null);
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
									| NoSuchMethodException | SecurityException | InstantiationException e) {
								// TODO Auto-generated catch block
								System.out.println("Exception Msg fallback:--"+ e.getMessage());
								e.printStackTrace();
							}
							return val;
				        });

				
		System.out.println(result.get());
		
		return joinPoint.proceed();
		
	}

}
