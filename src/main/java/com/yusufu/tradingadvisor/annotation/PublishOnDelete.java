package com.yusufu.tradingadvisor.annotation;

import org.springframework.context.ApplicationEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PublishOnDelete {
    Class<? extends ApplicationEvent> eventClass();

    Class<?> entityType();
}