package com.example.demo1.controller;

import com.example.demo1.exception.ResourceNotFoundException;
import com.example.demo1.model.Category;
import com.example.demo1.model.Product;
import com.example.demo1.service.CategoryService;
import com.example.demo1.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Product API Controller
 * RESTful API endpoints for product management
 */
@CrossOrigin
@RestController
@RequestMapping("/api/v2/products")
public class ProductApiController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    /**
     * Get all products
     * GET /api/products
     */
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    /**
     * Get product by ID
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        return ResponseEntity.ok(product);
    }

    /**
     * Create a new product
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        try {
            // Set category if categoryId is provided in the request
            if (product.getCategory() != null && product.getCategory().getId() != null) {
                Category category = categoryService.getCategoryById(product.getCategory().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
                product.setCategory(category);
            }

            Product createdProduct = productService.addProduct(product);
            return ResponseEntity.status(201).body(createdProduct);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid product data: " + e.getMessage());
        }
    }

    /**
     * Update an existing product
     * PUT /api/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product productDetails) {

        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        // Update all fields
        if (productDetails.getName() != null) {
            product.setName(productDetails.getName());
        }
        if (productDetails.getPrice() != null) {
            product.setPrice(productDetails.getPrice());
        }
        if (productDetails.getDescription() != null) {
            product.setDescription(productDetails.getDescription());
        }
        if (productDetails.getImageUrl() != null) {
            product.setImageUrl(productDetails.getImageUrl());
        }
        if (productDetails.getRating() != null) {
            product.setRating(productDetails.getRating());
        }
        if (productDetails.getDiscount() != null) {
            product.setDiscount(productDetails.getDiscount());
        }
        if (productDetails.getBrand() != null) {
            product.setBrand(productDetails.getBrand());
        }
        if (productDetails.getSpecs() != null) {
            product.setSpecs(productDetails.getSpecs());
        }
        if (productDetails.getCode() != null) {
            product.setCode(productDetails.getCode());
        }
        if (productDetails.getUpcoming() != null) {
            product.setUpcoming(productDetails.getUpcoming());
        }
        if (productDetails.getInstallmentPrice() != null) {
            product.setInstallmentPrice(productDetails.getInstallmentPrice());
        }
        if (productDetails.getDiscountedPrice() != null) {
            product.setDiscountedPrice(productDetails.getDiscountedPrice());
        }
        if (productDetails.getPromoStock() != null) {
            product.setPromoStock(productDetails.getPromoStock());
        }
        if (productDetails.getCategory() != null && productDetails.getCategory().getId() != null) {
            Category category = categoryService.getCategoryById(productDetails.getCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }

        Product updatedProduct = productService.updateProduct(product);

        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Delete a product
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {

        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        productService.deleteProductById(id);

        return ResponseEntity.noContent().build(); // 204 No Content - REST standard
    }
}
