package com.yusufu.javaspringfeatures.event.listener;

import com.yusufu.javaspringfeatures.event.EntityCreatedEvent;
import com.yusufu.javaspringfeatures.event.EntityDeletedEvent;
import com.yusufu.javaspringfeatures.model.entity.BaseEntity;
import com.yusufu.javaspringfeatures.model.entity.Product;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ProductEventListener {

    @Async
    @EventListener
    public void handleProductCreatedEventAsync(EntityCreatedEvent event) {
        Product createdProduct = event.getProduct();
        System.out.println("ProductEventListener EntityCreatedEvent (Async): ID: " + createdProduct.getId());
    }

    @Async
    @EventListener
    public void handleEntityDeletedEvent(EntityDeletedEvent<? extends BaseEntity> event) {
        System.out.println("ProductEventListener EntityDeletedEvent (Async): ID: " + event.getDeletedEntity().toString());
    }
}
