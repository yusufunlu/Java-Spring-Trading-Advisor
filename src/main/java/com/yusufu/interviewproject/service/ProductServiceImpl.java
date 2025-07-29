package com.yusufu.interviewproject.service;

import com.yusufu.interviewproject.annotation.PublishOnDelete;
import com.yusufu.interviewproject.event.EntityDeletedEvent;
import com.yusufu.interviewproject.event.EntityCreatedEvent;
import com.yusufu.interviewproject.model.entity.Product;
import com.yusufu.interviewproject.repo.ProductDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Primary
@Service
public class ProductServiceImpl implements ProductService{

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher eventPublisher;

    private ProductDao productDao;

    @Autowired
    public ProductServiceImpl(ApplicationEventPublisher eventPublisher, ProductDao productDao) {
        this.eventPublisher = eventPublisher;
        this.productDao = productDao;
    }

    @Override
    public List<Product> findAll() {
        return productDao.findAll();
    }

    @Override
    public Optional<Product> findById(String id) {
        return productDao.findById(id);
    }

    public Product saveOrUpdate(Product product) {
        String id = product.getId();
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
            product.setId(id);
        }
        ReentrantLock lock = locks.computeIfAbsent(id, k -> new ReentrantLock());
        lock.lock();

        try {
            Product savedProduct =  productDao.saveOrUpdateTransactional(product);
            eventPublisher.publishEvent(new EntityCreatedEvent(this, savedProduct));
            return savedProduct;
        } finally {
            lock.unlock();
            System.out.println(Thread.currentThread().getName() + " released lock on " + id);
            locks.compute(id, (k, v) -> (v != null && !v.hasQueuedThreads()) ? null : v);
        }
    }

    @PublishOnDelete(eventClass = EntityDeletedEvent.class, entityType = Product.class)
    @Override
    public Product delete(String id) {
        //Just for PublishOnDelete demo, otherise doesn't make sense
        Product product = productDao.findById(id).orElse(null);
        productDao.delete(id);
        return product;
    }
}
