package com.yusufu.javaspringfeatures;

import static org.junit.jupiter.api.Assertions.*;

import com.yusufu.javaspringfeatures.model.entity.Product;
import com.yusufu.javaspringfeatures.repo.ProductDao;
import com.yusufu.javaspringfeatures.repo.ProductRepository;
import com.yusufu.javaspringfeatures.service.ProductService;
import com.yusufu.javaspringfeatures.service.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.function.Function;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

@DataJpaTest
@Import({ProductDao.class, ProductServiceImpl.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    public void clearDb() {
        productRepository.deleteAll();
    }

    @Test
    public void testConcurrentSaveOrUpdate() throws InterruptedException {
        String sharedId = UUID.randomUUID().toString();
        int threadCount = 100;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        Set<String> finalNames = ConcurrentHashMap.newKeySet();

        Function<Integer, String> threadNameGenerator = n -> "Thread-" + n;

        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            executor.submit(() -> {
                Product p = Product.builder()
                        .id(sharedId)
                        .name(threadNameGenerator.apply(finalI))
                        .price(99.99)
                        .categoryId("cat-123")
                        .build();
                Product result = productService.saveOrUpdate(p);
                finalNames.add(result.getName());
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();

        Product finalProduct = productRepository.findById(sharedId).orElse(null);
        System.out.println("Final product in DB: " + finalProduct);
        assertEquals(threadNameGenerator.apply(threadCount-1), finalProduct.getName());
        assertTrue(finalNames.contains(finalProduct.getName()));
    }

    @Test
    public void testUpsert(){
        String id = new String("string3");
        Product p = Product.builder()
                .id(id)
                .name("Updated Name " + 3)
                .price(20.0 + 3)
                .categoryId("cat")
                .build();

        Product product = productService.saveOrUpdate(p);
        Product result = productRepository.findById(id).orElseThrow();

        assertEquals(id, result.getId());
    }
}

