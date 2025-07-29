package com.yusufu.interviewproject.event;

import com.yusufu.interviewproject.model.entity.BaseEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class EntityDeletedEvent<T extends BaseEntity> extends ApplicationEvent {

    private final T deletedEntity;
    public EntityDeletedEvent(Object source, T deletedEntity) {
        super(source);
        this.deletedEntity = deletedEntity;
    }

}
