package com.example.demo1.controller;

import com.example.demo1.model.Category;
import com.example.demo1.service.CartService;
import com.example.demo1.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@ControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
public class GlobalModelAttributes {
    private final CategoryService categoryService;
    private final CartService cartService;

    @ModelAttribute("headerAccessoryCameraCategories")
    public List<Category> headerAccessoryCameraCategories() {
        return categoryService.getAllCategories().stream()
                .filter(category -> isAccessoryGroup(category.getGroupName(), "camera"))
                .toList();
    }

    @ModelAttribute("headerAccessoryAudioCategories")
    public List<Category> headerAccessoryAudioCategories() {
        return categoryService.getAllCategories().stream()
                .filter(category -> isAccessoryGroup(category.getGroupName(), "thiet-bi-am-thanh"))
                .toList();
    }

    @ModelAttribute("headerAccessoryGamingCategories")
    public List<Category> headerAccessoryGamingCategories() {
        return categoryService.getAllCategories().stream()
                .filter(category -> isAccessoryGroup(category.getGroupName(), "phu-kien-gaming"))
                .toList();
    }

    @ModelAttribute("headerAccessoryOtherCategories")
    public List<Category> headerAccessoryOtherCategories() {
        return categoryService.getAllCategories().stream()
                .filter(category -> isAccessoryGroup(category.getGroupName(), "phu-kien-khac"))
                .toList();
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("cartItemCount")
    public int cartItemCount() {
        return cartService.getTotalItems();
    }

    private boolean isAccessoryGroup(String groupName, String expectedGroupKey) {
        if (groupName == null || groupName.isBlank()) {
            return false;
        }
        return normalizeGroup(groupName).equals(expectedGroupKey);
    }

    private String normalizeGroup(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
        return normalized.replaceAll("[^a-z0-9]+", "-");
    }
}
