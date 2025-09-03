package com.kevinpina.controllers;

import com.kevinpina.models.documents.Category;
import com.kevinpina.models.documents.Product;
import com.kevinpina.services.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@SessionAttributes("product") // used to recover the id in case of updating
@Controller
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    // Should move to Service (Logic)
    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    @Value("${config.upload.path}")
    private String path;

    @GetMapping({"/list", "/"})
    private String listProduct(Model model) {
        Flux<Product> products = productService.findAll();

        model.addAttribute("products", products);
        model.addAttribute("title", "List All Products");

        return "list";
    }

    @PostMapping("/form")
    // public Mono<String> save(Product product, SessionStatus sessionStatus) {
    //public Mono<String> save(@Valid @ModelAttribute("product") Product product, BindingResult result, Model model, SessionStatus sessionStatus) {
    public Mono<String> save(@Valid Product product, BindingResult result, Model model, @RequestPart("filePic") FilePart filePart, SessionStatus sessionStatus) {
        // 1. @Valid for Validating the fields of Product.class
        // 2. Always methodName(@Valid Product product, BindingResult result, and then the rest...) {}
        // 3. No need to use methodName(@Valid @ModelAttribute("product") Product product, and then the rest...) {}
        //    because the object Product is the same in model.addAttribute("product", ...);
        //    not the name of the variable is the Class.
    	// 4. If in form.html <input type="file" name="picture" /> @RequestPart FilePart picture; otherwise @RequestPart(name = "MyNameAttribute") FilePart picture

        if (result.hasErrors()) {
            model.addAttribute("title", "Form Product Errors");
            model.addAttribute("buttonName", "Save");
            return Mono.just("form");
        } else {
        	sessionStatus.setComplete(); // cleaning session
        }

        if (product.getCreateAt() == null) {
            product.setCreateAt(new Date());
        }

        if (!filePart.filename().isEmpty()) {
        	product.setPicture(UUID.randomUUID().toString() + "_" + filePart.filename()
        	.replace(" ", "")
        	.replace(":", "")
        	.replace("\\", ""));
        }

        Mono<Category> category = productService.findCategoryById(product.getCategory().getId());

        return category.flatMap(c -> {
			product.setCategory(c);
			return productService.save(product);
        })
        		.doOnNext(p -> log.info(p.toString()))
        		.flatMap(p -> {
        			 if (!filePart.filename().isEmpty()) {
        				 return filePart.transferTo(new File(path + p.getPicture()));
        			 } else {
        				 return Mono.empty();
        			 }
        		})
        		.thenReturn("redirect:/list?my_success=product+saved+successfully");

        //return productService.save(product)
        //        .doOnNext(p -> log.info(p.toString()))
        //        .then(Mono.just("redirect:/list"));
        //return productService.save(product)
        //        .doOnNext(p -> log.info(p.toString()))
        //        .thenReturn("redirect:/list?my_success=product+saved+successfully");
    }

    @GetMapping("/delete/{id}")
    public Mono<String> delete(@PathVariable String id) {
        return productService.findById(id)
                .defaultIfEmpty(new Product())  // In case {id} not found, used to avoid Product NullPointerException when getId()
                .flatMap(p -> {
                    if (p.getId() == null){
                        return Mono.error(new InterruptedException("Product to delete not exists"));
                    }
                    return Mono.just(p);
                })
                //.flatMap(productService::delete)
                .flatMap(p -> {
                    log.info("Deleting product: " + p.toString());
                    return productService.delete(p); // return Mono<Void>
                })
                .then(Mono.just("redirect:/list?my_success=product+deleted+successfully"))
                .onErrorResume(error -> Mono.just("redirect:/list?my_error=product+not+found"));
    }

    @GetMapping("/form")
    public Mono<String> create(Model model) {
        model.addAttribute("title", "Form Product");
        model.addAttribute("buttonName", "Add");
        model.addAttribute("product", new Product());   // Storing in session @SessionAttributes("product")
                                                                    // used to recover the id in case of updating
        return Mono.just("form");
    }

    @GetMapping("/form/v1/{id}")
    public Mono<String> editV1(@PathVariable String id, Model model) {
        Mono<Product> productMono = productService.findById(id)
                .doOnNext(p -> log.info(p.toString()))
                .defaultIfEmpty(new Product()); // In case {id} not found

        model.addAttribute("title", "Edit Product");
        model.addAttribute("buttonName", "Edit");
        model.addAttribute("product", productMono); // Storing in session @SessionAttributes("product")
                                                                // used to recover the id in case of updating
        return Mono.just("form");
    }

    @GetMapping("/form/v2/{id}")
    public Mono<String> editV2(@PathVariable(name = "id") String idValue, Model model) {
        return productService.findById(idValue)
                .doOnNext(p -> {
                    log.info(p.toString());
                    model.addAttribute("title", "Edit Product");
                    model.addAttribute("buttonName", "Edit");
                    model.addAttribute("product", p);   // NOT STORING in session @SessionAttributes("product")
                                                                    // because here we're using another process distinct of the Controller
                                                                    // that handles session. So we use in form.html hidden id.
                                                                    // NOTE: Works this in session in this new version!.
                })
                .defaultIfEmpty(new Product())  // In case {id} not found, used to avoid Product NullPointerException when getId()
                .flatMap(p -> {
                    if (p.getId() == null){
                        return Mono.error(new InterruptedException("Product not exists"));
                    }
                    return Mono.just(p);
                })
                .then(Mono.just("form"))
                .onErrorResume(error -> Mono.just("redirect:/list?my_error=product+not+found"));
    }

    @GetMapping("/list-datadriver1")
    private String listDataDriver1(Model model) {

        Flux<Product> products = productService.findByNameUpperCase()
                .delayElements(Duration.ofSeconds(1)); // list.html is delayed 1 second per each product

        // products.subscribe(product -> log.info(product.getName()));

        model.addAttribute("products", products);
        model.addAttribute("title", "List All Products");

        return "list";
    }

    /**
     * This method is used to work with Backpressure with Flux and Thymeleaf.
     * In this case the Buffer is configured in amount of Elements (2 in this case) instead of number Bytes.
     */
    @GetMapping("/list-datadriver2")
    private String listDataDriver2(Model model) {
        Collation collation = Collation.of("en");
        Query query = new Query().collation(collation);

        Flux<Product> products = mongoTemplate.find(query, Product.class)
                .map(product -> {
                    product.setName(product.getName().toUpperCase());
                    return product;
                })
                .delayElements(Duration.ofSeconds(1)); // list.html is delayed 1 second per every 2 product, defined 2 elements below

        model.addAttribute("products", new ReactiveDataDriverContextVariable(products, 2)); // Buffer with amount of elements 2
        model.addAttribute("title", "List All Products");

        return "list";
    }

    /**
     * Chunked is ideal for a lot of data.
     */
    @GetMapping("/list-full")
    private String listFull(Model model) {

        Flux<Product> products = productService.findByNameUpperCaseRepeat(); // Repeating 5000 times the current Flux.

        model.addAttribute("products", products);
        model.addAttribute("title", "List All Products");

        return "list";
    }

    /**
     * Chunked is ideal for a lot of data.
     */
    @GetMapping("/list-chunked")
    private String listChunked(Model model) {
        Collation collation = Collation.of("en");
        Query query = new Query().collation(collation);

        Flux<Product> products = mongoTemplate.find(query, Product.class)
                .map(product -> {
                    product.setName(product.getName().toUpperCase());
                    return product;
                })
                .repeat(5000); // Repeating 5000 times the current Flux.


        model.addAttribute("products", products);
        model.addAttribute("title", "List All Products");

        return "list-chunked";
    }

    @ModelAttribute("categories") // Used in form.html <option th:each="category: ${categories}"... 
    public Flux<Category> listCategory() {
        return productService.finAllCategory();
    }
    
    @GetMapping("/details/{id}")
    public Mono<String> getPic(Model model, @PathVariable String id) {
    	return productService.findById(id)
    			.doOnNext(p -> {
    				model.addAttribute("product", p);
    				model.addAttribute("title", "Product Details");
    			})
    			.switchIfEmpty(Mono.just(new Product()))
    			.flatMap(p -> {
    				if (p.getId() == null) {
    					return Mono.error(new InterruptedException("Product does not exists"));
    				} else {
    					return Mono.just(p);
    				}
    			})
    			.then(Mono.just("details"))
    			.onErrorResume(ex -> Mono.just("redirect:/list?my_error=product+not+found"));
    }
    
    @GetMapping("/upload/img/{picName:.+}") //.+ any extension Picture file
    public Mono<ResponseEntity<Resource>> seePic(@PathVariable String picName) throws MalformedURLException {
    	Path pathFile = Paths.get(path).resolve(picName).toAbsolutePath();
    	
    	Resource image = new UrlResource(pathFile.toUri());
    	
    	return Mono.just(
    			ResponseEntity.ok()
    				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFilename() + "\"")
    				.body(image));
    }

}
