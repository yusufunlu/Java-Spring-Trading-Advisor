package com.yusufu.javaspringfeatures.event;

import com.yusufu.javaspringfeatures.model.entity.Product;
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
