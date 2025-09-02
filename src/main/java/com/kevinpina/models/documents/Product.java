package com.kevinpina.models.documents;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Document(collection = "product", collation = "en") // SQL @Entity, NoSQL @Document
public class Product {

    @Id
    private String id;

    @NonNull // Used for Constructor required @RequiredArgsConstructor
    @NotEmpty // Used for validation in ProductController.save(..., BindingResult result,...);
    private String name;

    @NonNull // Used for Constructor required @RequiredArgsConstructor
    @jakarta.validation.constraints.NotNull // Used for validation in ProductController.save(..., BindingResult result,...);
    private Float price;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createAt;

    @Valid // Used for validation in ProductController.save(..., BindingResult result,...);
    @NonNull // Used for Constructor required @RequiredArgsConstructor
    private Category category;
    
    private String picture;

}
