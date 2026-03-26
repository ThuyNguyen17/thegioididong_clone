package com.example.demo1.controller;

import com.example.demo1.model.Category;
import com.example.demo1.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CategoryController {
    private static final String CATEGORY_FORM_VIEW = "/categories/add-category";
    private static final String CATEGORY_UPDATE_VIEW = "/categories/update-category";
    private static final Path IMAGE_UPLOAD_DIR = Paths.get("src", "main", "resources", "static", "images")
            .toAbsolutePath()
            .normalize();

    @Autowired
    private final CategoryService categoryService;

    @GetMapping("/categories/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        return CATEGORY_FORM_VIEW;
    }

    @PostMapping("/categories/add")
    public String addCategory(
            @Valid Category category,
            BindingResult result,
            @RequestParam("imageFile") MultipartFile imageFile
    ) {
        if (result.hasErrors()) {
            return CATEGORY_FORM_VIEW;
        }
        handleCategoryImage(category, imageFile, null);
        categoryService.addCategory(category);
        return "redirect:/categories";
    }

    @GetMapping("/categories")
    public String listCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "/categories/categories-list";
    }

    @GetMapping("/categories/edit/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category Id:" + id));
        model.addAttribute("category", category);
        return CATEGORY_UPDATE_VIEW;
    }

    @PostMapping("/categories/update/{id}")
    public String updateCategory(
            @PathVariable("id") Long id,
            @Valid Category category,
            BindingResult result,
            @RequestParam("imageFile") MultipartFile imageFile,
            Model model
    ) {
        Category existingCategory = categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category Id:" + id));

        if (result.hasErrors()) {
            category.setId(id);
            return CATEGORY_UPDATE_VIEW;
        }

        category.setId(id);
        handleCategoryImage(category, imageFile, existingCategory.getImageUrl());
        categoryService.updateCategory(category);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "redirect:/categories";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, Model model) {
        categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category Id:" + id));
        categoryService.deleteCategoryById(id);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "redirect:/categories";
    }

    private void handleCategoryImage(Category category, MultipartFile imageFile, String fallbackImageUrl) {
        String imageUrl = fallbackImageUrl;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                Files.createDirectories(IMAGE_UPLOAD_DIR);
                String originalFileName = imageFile.getOriginalFilename() == null ? "category-image" : imageFile.getOriginalFilename();
                String cleanFileName = StringUtils.cleanPath(originalFileName);
                String fileName = System.currentTimeMillis() + "_" + cleanFileName;
                Path filePath = IMAGE_UPLOAD_DIR.resolve(fileName).normalize();
                Files.write(filePath, imageFile.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                imageUrl = fileName;
            } catch (Exception e) {
                throw new IllegalStateException("Khong the luu hinh category", e);
            }
        }
        category.setImageUrl(imageUrl);
    }
}
