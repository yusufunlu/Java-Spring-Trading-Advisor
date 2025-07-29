package com.yusufu.interviewproject.annotation.AOP;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class LoggingAspect {

    //to use the annotation on method you need to use  annotation instead of within
    //Do not read body since if it is InputStream it can be read only once
    //if you want to read InputStream bodies you need to use filters
    @Before("@within(com.yusufu.interviewproject.annotation.EnableLog)")
    public void logRequestInfo(JoinPoint joinPoint) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();

            String method = request.getMethod();
            String uri = request.getRequestURI();
            String query = request.getQueryString();

            System.out.println("********Incoming request*******");
            System.out.println("Method: " + method);
            System.out.println("Path: " + uri);
            if (query != null) {
                System.out.println("➡️ Query: " + query);
            }

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String methodName = signature.getMethod().getName();
            System.out.println("🧩 Handler Method: " + methodName);
            System.out.println("**********************");
        }
    }
}
