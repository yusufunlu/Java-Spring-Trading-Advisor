package com.yusufu.javaspringfeatures.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.*;


@Entity
@Table(name = "PRODUCT")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA needs this
@AllArgsConstructor
@Builder
public class Order extends BaseEntity{
    @Id
    protected String id;
    private String name;
    private Double price;
    private String categoryId;
    @Transient
    private String categoryName;

}
