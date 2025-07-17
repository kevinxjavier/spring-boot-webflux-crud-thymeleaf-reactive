package com.kevinpina.models.dao;

import com.kevinpina.models.documents.Category;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoryDao extends ReactiveMongoRepository<Category, String> {

}
