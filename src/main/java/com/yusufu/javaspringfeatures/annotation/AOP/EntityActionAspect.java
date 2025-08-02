package com.yusufu.javaspringfeatures.annotation.AOP;

import com.yusufu.javaspringfeatures.annotation.PublishOnDelete;
import com.yusufu.javaspringfeatures.event.EntityDeletedEvent;
import com.yusufu.javaspringfeatures.model.entity.BaseEntity;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class EntityActionAspect {

    private final ApplicationEventPublisher eventPublisher;

    @AfterReturning(pointcut = "@annotation(publishOnDelete)", returning = "result")
    public void publishEventAfterDelete(JoinPoint joinPoint, PublishOnDelete publishOnDelete, Object result) {
        if (result == null || !(result instanceof BaseEntity)) {
            System.err.println("EventPublishingAspect Error");
            return;
        }
        ApplicationEvent event = new EntityDeletedEvent(joinPoint.getTarget(), (BaseEntity) result);
        eventPublisher.publishEvent(event);
    }
}
