package com.yusufu.interviewproject.repo;

import com.yusufu.interviewproject.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    @Query(value = """
    SELECT p.id, p.name, p.price, p.category_id, c.name as category_name
    FROM product p
    LEFT JOIN category c ON p.category_id = c.id
    WHERE p.id = :id
""", nativeQuery = true)
    List<Object[]> findProductWithCategoryNameNative(@Param("id") String id);
}
