package com.kevinpina.models.documents;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.BindingResult;

import java.util.Date;

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


}
