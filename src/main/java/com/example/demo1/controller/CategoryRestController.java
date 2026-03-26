package com.example.demo1.controller;

import com.example.demo1.exception.ResourceNotFoundException;
import com.example.demo1.model.Category;
import com.example.demo1.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Category REST API Controller
 * RESTful API endpoints for category management
 */
@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryRestController {
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * Get all categories
     * GET /api/categories
     */
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
    
    /**
     * Get category by ID
     * GET /api/categories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }
    
    /**
     * Create a new category
     * POST /api/categories
     */
    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        try {
            if (category.getName() == null || category.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Category name is required");
            }
            categoryService.addCategory(category);
            return ResponseEntity.status(201).body(category);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid category data: " + e.getMessage());
        }
    }
    
    /**
     * Update an existing category
     * PUT /api/categories/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @RequestBody Category categoryDetails) {
        
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        
        if (categoryDetails.getName() != null && !categoryDetails.getName().trim().isEmpty()) {
            category.setName(categoryDetails.getName());
        }
        if (categoryDetails.getGroupName() != null) {
            category.setGroupName(categoryDetails.getGroupName());
        }
        if (categoryDetails.getImageUrl() != null) {
            category.setImageUrl(categoryDetails.getImageUrl());
        }
        
        categoryService.updateCategory(category);
        return ResponseEntity.ok(category);
    }
    
    /**
     * Delete a category
     * DELETE /api/categories/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        
        categoryService.deleteCategoryById(id);
        return ResponseEntity.noContent().build(); // 204 No Content - REST standard
    }
}
