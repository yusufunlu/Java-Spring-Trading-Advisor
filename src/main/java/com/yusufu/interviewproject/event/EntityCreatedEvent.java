package com.yusufu.interviewproject.event;

import com.yusufu.interviewproject.model.entity.Product;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class EntityCreatedEvent extends ApplicationEvent {

    private final Product product;

    public EntityCreatedEvent(Object source, Product product) {
        super(source);
        this.product = product;
    }
}
