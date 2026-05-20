package com.my_space.device_service.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(* com.my_space.device_service.service.*.*(..))")
    public void serviceMethods() {
    }

    @Before("serviceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("Service Method {} called with args {}", joinPoint.getSignature().getName(), joinPoint.getArgs());
    }

    @After("serviceMethods()")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("Service Method Returned: #{} : {}", joinPoint.getSignature().getName(), result);
    }
}
