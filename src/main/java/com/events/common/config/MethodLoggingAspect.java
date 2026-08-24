package com.events.common.config;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class MethodLoggingAspect {

    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";

    @Around("@within(org.springframework.stereotype.Service)")
    public Object logMethods(ProceedingJoinPoint joinPoint) throws Throwable {

        String caller = getCaller();

        log.info("{}➡️ {} appelle {}{}",
                GREEN,
                caller,
                joinPoint.getSignature().toShortString(),
                RESET
        );

        return joinPoint.proceed();
    }


    private String getCaller() {

        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        for (StackTraceElement element : stack) {

            String className = element.getClassName();

            if (
                    !className.contains("MethodLoggingAspect") &&
                            !className.contains("$$SpringCGLIB$$") &&
                            !className.startsWith("java.") &&
                            !className.startsWith("jdk.") &&
                            !className.startsWith("org.springframework.")
            ) {
                return className.substring(className.lastIndexOf('.') + 1)
                        + "."
                        + element.getMethodName();
            }
        }


        return "unknown";
    }
}