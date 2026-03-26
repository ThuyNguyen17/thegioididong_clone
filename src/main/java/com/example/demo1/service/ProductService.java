package com.example.demo1.service;

import com.example.demo1.model.Product;
import com.example.demo1.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private final ProductRepository productRepository;

    // Retrieve all products from the database
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Retrieve a product by its id
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // Add a new product to the database
    public Product addProduct(Product product) {
        normalizePricing(product);
        return productRepository.save(product);
    }

    // Update an existing product
    public Product updateProduct(@NotNull Product product) {
        Product existingProduct = productRepository.findById(product.getId())
                .orElseThrow(() -> new IllegalStateException("Product with ID " +
                        product.getId() + " does not exist."));
        existingProduct.setName(product.getName());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setImageUrl(product.getImageUrl());
        existingProduct.setRating(product.getRating());
        existingProduct.setDiscount(product.getDiscount());
        existingProduct.setBrand(product.getBrand());
        existingProduct.setSpecs(product.getSpecs());
        existingProduct.setCode(product.getCode());
        existingProduct.setUpcoming(product.getUpcoming());
        existingProduct.setInstallmentPrice(product.getInstallmentPrice());
        existingProduct.setDiscountedPrice(product.getDiscountedPrice());
        existingProduct.setPromoStock(product.getPromoStock());

        // Keep promoTotal internal: auto-managed in normalizePricing().

        normalizePricing(existingProduct);
        return productRepository.save(existingProduct);
    }

    // Delete a product by its id
    public void deleteProductById(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalStateException("Product with ID " + id + " does not exist.");
        }
        productRepository.deleteById(id);
    }

    private void normalizePricing(Product product) {

        // Validate discounted price
        if (product.getDiscountedPrice() == null ||
                product.getDiscountedPrice() <= 0 ||
                product.getDiscountedPrice() >= product.getPrice()) {
            product.setDiscountedPrice(null);
        }

        // Validate promoStock
        if (product.getPromoStock() == null || product.getPromoStock() < 0) {
            product.setPromoStock(0);
        }

        Integer promoTotal = product.getPromoTotal();
        boolean promoActive = product.getDiscountedPrice() != null;

        if (promoActive) {

            // 👉 Nếu lần đầu tạo (chưa có total) thì set total = promoStock ban đầu
            if (promoTotal == null || promoTotal <= 0) {
                product.setPromoTotal(product.getPromoStock());
            }

            // 👉 Không cho promoStock vượt quá promoTotal
            if (product.getPromoStock() > product.getPromoTotal()) {
                product.setPromoStock(product.getPromoTotal());
            }

        } else {

            // Không có khuyến mãi
            product.setDiscountedPrice(null);
            product.setPromoStock(0);

            if (product.getPromoTotal() == null) {
                product.setPromoTotal(0);
            }
        }
    }
}
