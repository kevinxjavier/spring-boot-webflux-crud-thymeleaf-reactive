package com.kevinpina.models.documents;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotEmpty;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Document(collection = "category")
public class Category {

	@NotEmpty // Used for validation in ProductController.save(..., BindingResult result,...);
    private String id;

    @NonNull
    private String name;

}
