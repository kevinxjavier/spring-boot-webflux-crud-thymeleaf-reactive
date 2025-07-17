package com.kevinpina.models.dao;

import com.kevinpina.models.documents.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductDao extends ReactiveMongoRepository<Product, String> {

}
