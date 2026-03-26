package com.example.demo1.controller;

import com.example.demo1.model.Category;
import com.example.demo1.model.Product;
import com.example.demo1.service.CategoryService;
import com.example.demo1.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductRestController {

    private static final Path IMAGE_UPLOAD_DIR = Paths.get("src", "main", "resources", "static", "images")
            .toAbsolutePath()
            .normalize();
    
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productService.getAllProducts().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
                .toList();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String q) {
        List<Product> products = productService.getAllProducts().stream()
                .filter(p -> p.getName().toLowerCase().contains(q.toLowerCase()) ||
                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(q.toLowerCase())) ||
                            (p.getBrand() != null && p.getBrand().toLowerCase().contains(q.toLowerCase())))
                .toList();
        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody ProductCreateRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty() || request.getPrice() == null || request.getPrice() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        Product product = new Product();
        product.setName(request.getName().trim());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setRating(request.getRating());
        product.setBrand(request.getBrand());
        product.setSpecs(request.getSpecs());
        product.setCode(request.getCode());
        product.setUpcoming(Boolean.TRUE.equals(request.getUpcoming()));
        product.setInstallmentPrice(request.getInstallmentPrice());
        product.setDiscount(request.getDiscount());
        product.setPromoStock(request.getPromoStock());
        product.setPromoTotal(request.getPromoTotal());

        if (request.getDiscountedPrice() != null && request.getDiscountedPrice() > 0 && request.getDiscountedPrice() < request.getPrice()) {
            product.setDiscountedPrice(request.getDiscountedPrice());
        } else {
            product.setDiscountedPrice(null);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(request.getCategoryId()).orElse(null);
            product.setCategory(category);
        }

        Product savedProduct = productService.addProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody ProductCreateRequest request) {
        Product existingProduct = productService.getProductById(id).orElse(null);
        if (existingProduct == null) {
            return ResponseEntity.notFound().build();
        }

        existingProduct.setName(request.getName() != null ? request.getName().trim() : null);
        existingProduct.setPrice(request.getPrice());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setImageUrl(request.getImageUrl());
        existingProduct.setRating(request.getRating());
        existingProduct.setBrand(request.getBrand());
        existingProduct.setSpecs(request.getSpecs());
        existingProduct.setCode(request.getCode());
        existingProduct.setUpcoming(Boolean.TRUE.equals(request.getUpcoming()));
        existingProduct.setInstallmentPrice(request.getInstallmentPrice());
        existingProduct.setDiscount(request.getDiscount());
        existingProduct.setPromoStock(request.getPromoStock());

        if (request.getDiscountedPrice() != null && request.getDiscountedPrice() > 0 && request.getDiscountedPrice() < request.getPrice()) {
            existingProduct.setDiscountedPrice(request.getDiscountedPrice());
        } else {
            existingProduct.setDiscountedPrice(null);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(request.getCategoryId()).orElse(null);
            existingProduct.setCategory(category);
        } else {
            existingProduct.setCategory(null);
        }

        Product updatedProduct = productService.updateProduct(existingProduct);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (!productService.getProductById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        productService.deleteProductById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadProductImage(@RequestParam("imageFile") MultipartFile imageFile) {
        try {
            if (imageFile == null || imageFile.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            String contentType = imageFile.getContentType();
            if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                return ResponseEntity.badRequest().build();
            }

            Files.createDirectories(IMAGE_UPLOAD_DIR);

            String originalFileName = imageFile.getOriginalFilename() == null ? "product-image" : imageFile.getOriginalFilename();
            String cleanFileName = StringUtils.cleanPath(originalFileName);
            String fileName = System.currentTimeMillis() + "_" + cleanFileName;

            Path filePath = IMAGE_UPLOAD_DIR.resolve(fileName).normalize();
            // Prevent path traversal
            if (!filePath.startsWith(IMAGE_UPLOAD_DIR)) {
                return ResponseEntity.badRequest().build();
            }

            Files.write(filePath, imageFile.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            return ResponseEntity.ok(new ImageUploadResponse(fileName));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public static class ImageUploadResponse {
        private String imageUrl;

        public ImageUploadResponse() {
        }

        public ImageUploadResponse(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

    public static class ProductCreateRequest {
        private String name;
        private Double price;
        private String description;
        private String imageUrl;
        private Integer stock;
        private Double rating;
        private Integer discount;
        private String brand;
        private String specs;
        private String code;
        private Boolean upcoming;
        private Double installmentPrice;
        private Double discountedPrice;
        private Integer promoStock;
        private Integer promoTotal;
        private Long categoryId;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public Double getRating() {
            return rating;
        }

        public void setRating(Double rating) {
            this.rating = rating;
        }

        public Integer getDiscount() {
            return discount;
        }

        public void setDiscount(Integer discount) {
            this.discount = discount;
        }

        public String getBrand() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

        public String getSpecs() {
            return specs;
        }

        public void setSpecs(String specs) {
            this.specs = specs;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Boolean getUpcoming() {
            return upcoming;
        }

        public void setUpcoming(Boolean upcoming) {
            this.upcoming = upcoming;
        }

        public Double getInstallmentPrice() {
            return installmentPrice;
        }

        public void setInstallmentPrice(Double installmentPrice) {
            this.installmentPrice = installmentPrice;
        }

        public Double getDiscountedPrice() {
            return discountedPrice;
        }

        public void setDiscountedPrice(Double discountedPrice) {
            this.discountedPrice = discountedPrice;
        }

        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }

        public Integer getPromoStock() {
            return promoStock;
        }

        public void setPromoStock(Integer promoStock) {
            this.promoStock = promoStock;
        }

        public Integer getPromoTotal() {
            return promoTotal;
        }

        public void setPromoTotal(Integer promoTotal) {
            this.promoTotal = promoTotal;
        }
    }
}
