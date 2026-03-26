package com.example.demo1.controller;

import com.example.demo1.model.Product;
import com.example.demo1.service.CartService;
import com.example.demo1.service.CategoryService;
import com.example.demo1.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @GetMapping("/")
    public String home(Model model) {
        List<Product> products = productService.getAllProducts().stream()
                // Discounted products are treated as limited-stock promo items.
                // Hide them when promoStock is empty.
                .filter(product -> {
                    if (product.isDiscounted()) {
                        Integer promoStock = product.getPromoStock();
                        return promoStock != null && promoStock > 0;
                    }
                    return true;
                })
                .toList();

        List<Product> discountedProducts = products.stream()
                .filter(Product::isDiscounted)
                .toList();

        List<Product> normalProducts = products.stream()
                .filter(product -> !product.isDiscounted())
                .toList();

        Map<Long, Integer> cartQuantities = cartService.getCartItems().stream()
                .collect(Collectors.toMap(item -> item.getProduct().getId(), CartItem::getQuantity));

        model.addAttribute("products", products);
        model.addAttribute("discountedProducts", discountedProducts);
        model.addAttribute("normalProducts", normalProducts);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("cartQuantities", cartQuantities);
        return "home/index";
    }
    @GetMapping("/403")
    public String accessDenied() {
        return "error/403";
    }
}
