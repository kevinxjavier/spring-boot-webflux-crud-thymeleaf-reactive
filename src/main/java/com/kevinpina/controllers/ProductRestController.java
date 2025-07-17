package com.kevinpina.controllers;

import com.kevinpina.models.dao.ProductDao;
import com.kevinpina.models.documents.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {

    private static final Logger log = LoggerFactory.getLogger(ProductRestController.class);

    // Field Injection
    //@Autowired
    //private ProductDao productDao;

    //@Autowired
    //private ReactiveMongoTemplate mongoTemplate;

    private final ProductDao productDao;
    private final ReactiveMongoTemplate mongoTemplate;

    // Constructor Injection
    public ProductRestController(ProductDao productDao, ReactiveMongoTemplate mongoTemplate) {
        this.productDao = productDao;
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping
    public Flux<Product> getAll() {
        return productDao.findAll()
                .map(product -> {
                    product.setName(product.getName().toUpperCase());
                    return product;
                })
                .doOnNext(product -> log.info(product.toString()));
    }

    @GetMapping("/v1/id/{id}")
    public Mono<Product> getIdV1(@PathVariable String id) {
        return productDao.findById(id);
    }

    @GetMapping("/v2/id/{id}")
    public Mono<Product> getIdV2(@PathVariable String id) {
        return productDao.findAll()
                .map(product -> {
                    product.setName(product.getName().toUpperCase());
                    return product;
                })
                .filter(product -> product.getId().equals(id))
                .next()
                .doOnNext(product -> log.info(product.toString()));
    }
}
