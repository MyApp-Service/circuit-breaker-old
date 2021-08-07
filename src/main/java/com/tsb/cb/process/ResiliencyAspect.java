package com.tsb.cb.process;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.tsb.cb.annotation.CircuitBreakerTSB;
import com.tsb.cb.annotation.TsbCircuitBreaker;
import com.tsb.cb.config.CircuitBreakerConfigurationProperties;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import io.github.resilience4j.core.lang.Nullable;



@Aspect
@Component
public class ResiliencyAspect {
	
	//private final CircuitBreakerConfigurationProperties circuitBreakerProperties=new CircuitBreakerConfigurationProperties();
	//private  CircuitBreakerRegistry circuitBreakerRegistry;
    /*private  @Nullable
    List<CircuitBreakerAspectExt> circuitBreakerAspectExtList;*/
	@Autowired
    private  FallbackDecorators fallbackDecorators;
    //private final SpelResolver spelResolver;
    
   
    
    public ResiliencyAspect(
			/* CircuitBreakerRegistry circuitBreakerRegistry, */
			/* List<CircuitBreakerAspectExt> circuitBreakerAspectExtList, */
			/* FallbackDecorators fallbackDecorators */
            ) {
		
				/* this.circuitBreakerRegistry = circuitBreakerRegistry; */
				/* this.circuitBreakerAspectExtList = circuitBreakerAspectExtList; */
				/* this.fallbackDecorators = fallbackDecorators; */
		//this.spelResolver = spelResolver;
	}
    
    
    
    
	
	 @Around(value = " @annotation(com.tsb.cb.annotation.CircuitBreakerTSB)")
	    public Object circuitBreakerAroundAdvice(ProceedingJoinPoint proceedingJoinPoint
	         ) throws Throwable {
		 
		 System.out.println("circuitBreakerAroundAdvice---");
	        Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();
	        String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
	        
	        Class cls = proceedingJoinPoint.getTarget().getClass();
			
			Method m = cls.getMethod(proceedingJoinPoint.getSignature().getName());
			
			CircuitBreakerTSB circuitBreakerAnnotation = m.getAnnotation(CircuitBreakerTSB.class);
			
	        
	        if (circuitBreakerAnnotation == null) {
	            circuitBreakerAnnotation = getCircuitBreakerAnnotation(proceedingJoinPoint);
	        }
	        if (circuitBreakerAnnotation == null) { //because annotations wasn't found
	            return proceedingJoinPoint.proceed();
	        }
	        //String backend = spelResolver.resolve(method, proceedingJoinPoint.getArgs(), circuitBreakerAnnotation.name());
	       CircuitBreaker circuitBreaker = getOrCreateCircuitBreaker(
	            methodName, circuitBreakerAnnotation.name());
	        Class<?> returnType = method.getReturnType();

			/*
			 * String fallbackMethodValue = spelResolver.resolve(method,
			 * proceedingJoinPoint.getArgs(), circuitBreakerAnnotation.fallbackMethod()); if
			 * (StringUtils.isEmpty(fallbackMethodValue)) { return
			 * proceed(proceedingJoinPoint, methodName, circuitBreaker, returnType); }
			 */
	        FallbackMethod fallbackMethod = FallbackMethod
	            .create(circuitBreakerAnnotation.fallback(), method,
	                proceedingJoinPoint.getArgs(), proceedingJoinPoint.getTarget());
	        return fallbackDecorators.decorate(fallbackMethod,
	            () -> proceed(proceedingJoinPoint, methodName, circuitBreaker, returnType)).apply();
	    }
	 
	 private Object proceed(ProceedingJoinPoint proceedingJoinPoint, String methodName,
		        io.github.resilience4j.circuitbreaker.CircuitBreaker circuitBreaker, Class<?> returnType)
		        throws Throwable {
		        /*if (circuitBreakerAspectExtList != null && !circuitBreakerAspectExtList.isEmpty()) {
		            for (CircuitBreakerAspectExt circuitBreakerAspectExt : circuitBreakerAspectExtList) {
		                if (circuitBreakerAspectExt.canHandleReturnType(returnType)) {
		                    return circuitBreakerAspectExt
		                        .handle(proceedingJoinPoint, circuitBreaker, methodName);
		                }
		            }
		        }*/
		        if (CompletionStage.class.isAssignableFrom(returnType)) {
		            return handleJoinPointCompletableFuture(proceedingJoinPoint, circuitBreaker);
		        }
		        return defaultHandling(proceedingJoinPoint, circuitBreaker);
		    }

		    private CircuitBreaker getOrCreateCircuitBreaker(
		        String methodName, String backend) {
				/*
				 * CircuitBreaker circuitBreaker = circuitBreakerRegistry
				 * .circuitBreaker(backend);
				 */
		    	
		    	CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
		    		    .failureRateThreshold(50)
		    		    .waitDurationInOpenState(Duration.ofMillis(1000))
		    		    .permittedNumberOfCallsInHalfOpenState(10)
		    		    .slidingWindowSize(2)
		    		    .recordExceptions(IOException.class, TimeoutException.class)
		    		    .build();

		    		// Create a CircuitBreakerRegistry with a custom global configuration
		    		CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(circuitBreakerConfig);
		    		
		    		CircuitBreaker circuitBreaker = registry.circuitBreaker(backend);

				/*
				 * if (logger.isDebugEnabled()) { logger.debug(
				 * "Created or retrieved circuit breaker '{}' with failure rate '{}' for method: '{}'"
				 * , backend,
				 * circuitBreaker.getCircuitBreakerConfig().getFailureRateThreshold(),
				 * methodName); }
				 */

		        return circuitBreaker;
		    }

		    @Nullable
		    private CircuitBreakerTSB getCircuitBreakerAnnotation(ProceedingJoinPoint proceedingJoinPoint) {
				
		    	
		        if (proceedingJoinPoint.getTarget() instanceof Proxy) {
		            
		            return AnnotationExtractor
		                .extractAnnotationFromProxy(proceedingJoinPoint.getTarget(), CircuitBreakerTSB.class);
		        } else {
		            return AnnotationExtractor
		                .extract(proceedingJoinPoint.getTarget().getClass(), CircuitBreakerTSB.class);
		        }
		    	
		    }

		    /**
		     * handle the CompletionStage return types AOP based into configured circuit-breaker
		     */
		    private Object handleJoinPointCompletableFuture(ProceedingJoinPoint proceedingJoinPoint,
		        CircuitBreaker circuitBreaker) {
		        return circuitBreaker.executeCompletionStage(() -> {
		            try {
		                return (CompletionStage<?>) proceedingJoinPoint.proceed();
		            } catch (Throwable throwable) {
		                throw new CompletionException(throwable);
		            }
		        });
		    }

		    /**
		     * the default Java types handling for the circuit breaker AOP
		     */
		    private Object defaultHandling(ProceedingJoinPoint proceedingJoinPoint,
		        CircuitBreaker circuitBreaker) throws Throwable {
		        return circuitBreaker.executeCheckedSupplier(proceedingJoinPoint::proceed);
		    }

			/*
			 * @Override public int getOrder() { return
			 * circuitBreakerProperties.getCircuitBreakerAspectOrder(); }
			 */
}
