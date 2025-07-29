package com.yusufu.interviewproject.repo;

import com.yusufu.interviewproject.model.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductDao {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ProductRepository productRepository;
    public List<Product> findAll() {
        return productRepository.findAll();
    }
    public Optional<Product> findById(String id) {
        List<Object[]> result = productRepository.findProductWithCategoryNameNative(id);
        if (result.isEmpty()) return Optional.empty();

        Object[] row = result.get(0);
        return Optional.of(new Product(
                (String) row[0],
                (String) row[1],
                (Double) row[2],
                (String) row[3],
                (String) row[4]
        ));
    }

    public void delete(String id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public Product saveOrUpdateTransactional(Product product) {
        Product existing = em.find(Product.class, product.getId());

        if (existing != null) {
            existing.setName(product.getName());
            existing.setPrice(product.getPrice());
            existing.setCategoryId(product.getCategoryId());
            return em.merge(existing);
        } else {
            em.persist(product);
            return product;
        }
    }
}
