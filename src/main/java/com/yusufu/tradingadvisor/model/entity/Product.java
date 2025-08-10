package com.yusufu.tradingadvisor.model.entity;

import jakarta.persistence.*;
import lombok.*;



@Entity
@Table(name = "PRODUCT")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA needs this
@AllArgsConstructor
@Builder
@ToString
public class Product extends BaseEntity{
    @Id
    protected String id;
    private String name;
    private Double price;
    private String categoryId;
    @Transient
    private String categoryName;

}
