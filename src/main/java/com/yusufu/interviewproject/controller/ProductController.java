package com.yusufu.interviewproject.controller;

import com.yusufu.interviewproject.annotation.EnableLog;
import com.yusufu.interviewproject.model.entity.Product;
import com.yusufu.interviewproject.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@EnableLog
@RestController
@RequestMapping(path = "/api/product")
public class ProductController {

    private final ProductService productService;

    @Autowired // No needed since Spring 4.3+ if single constructor
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getAll() {
        return Optional.ofNullable(productService.findAll())
                .orElse(Collections.emptyList());
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable String id) {
        return productService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No product found"));
    }

    //TODO: think about validation
    @PostMapping
    public void create(@RequestBody Product product) {
        productService.saveOrUpdate(product);
    }

    //TODO: think about if you need seperate id and idompotent
    @PutMapping("/{id}")
    public void update(@RequestBody Product updatedProduct) {
        productService.saveOrUpdate(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        productService.delete(id);
    }

}
