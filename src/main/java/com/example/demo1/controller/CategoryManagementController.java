package com.example.demo1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/categories/ajax")
public class CategoryManagementController {

    @GetMapping
    public String categoryManagement() {
        return "categories/category-ajax";
    }
}
