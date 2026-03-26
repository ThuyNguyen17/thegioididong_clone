package com.example.demo1.controller;

import com.example.demo1.model.Product;
import com.example.demo1.service.CategoryService;
import com.example.demo1.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Controller
@RequestMapping({"/product", "/products"})
public class ProductController {
    private static final Path IMAGE_UPLOAD_DIR = Paths.get("src", "main", "resources", "static", "images")
            .toAbsolutePath()
            .normalize();

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @GetMapping
    public String showProductList(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "products/product-list";
    }

    @GetMapping("/ajax")
    public String showProductAjax(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products/product-ajax";
    }
    // For adding a new product
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products/add-product";
    }
    // Process the form for adding a new product
    @PostMapping("/add")
    public String addProduct(
            @Valid Product product,
            BindingResult result,
            @RequestParam("imageFile") MultipartFile imageFile,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "products/add-product";
        }

        if (!imageFile.isEmpty()) {
            try {
                product.setImageUrl(saveImage(imageFile));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        productService.addProduct(product);
        return "redirect:/product";
    }

    // For editing a product
// For editing a product
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "/products/update-product";
    }
    // Process the form for updating a product
    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id, @Valid Product product,
                                BindingResult result,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                Model model) {
        Product existingProduct = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));

        if (result.hasErrors()) {
            product.setId(id);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "/products/update-product";
        }

        product.setId(id);
        product.setImageUrl(existingProduct.getImageUrl());
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                product.setImageUrl(saveImage(imageFile));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        productService.updateProduct(product);
        return "redirect:/product";
    }

    // Handle request to delete a product
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProductById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa sản phẩm này vì đang có đơn hàng liên quan!");
        }
        return "redirect:/product";
    }

    // Handle request to view a product detail
    @GetMapping("/detail/{id}")
    public String viewProductDetail(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        return "products/product-detail";
    }

    private String saveImage(MultipartFile imageFile) throws Exception {
        Files.createDirectories(IMAGE_UPLOAD_DIR);
        String originalFileName = imageFile.getOriginalFilename() == null
                ? "product-image"
                : imageFile.getOriginalFilename();
        String cleanFileName = StringUtils.cleanPath(originalFileName);
        String fileName = System.currentTimeMillis() + "_" + cleanFileName;
        Path filePath = IMAGE_UPLOAD_DIR.resolve(fileName).normalize();

        Files.write(filePath, imageFile.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return fileName;
    }
}
