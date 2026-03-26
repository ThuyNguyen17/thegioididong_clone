package com.example.demo1.service;

import com.example.demo1.controller.CartItem;
import com.example.demo1.model.Product;
import com.example.demo1.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;

@Service
@SessionScope
public class CartService {
    private List<CartItem> cartItems = new ArrayList<>();
    private com.example.demo1.model.Voucher appliedVoucher;

    @Autowired
    private ProductRepository productRepository;

    public com.example.demo1.model.Voucher getAppliedVoucher() {
        return appliedVoucher;
    }

    public void setAppliedVoucher(com.example.demo1.model.Voucher voucher) {
        this.appliedVoucher = voucher;
    }

    public double getVoucherDiscount() {
        return appliedVoucher != null ? appliedVoucher.getDiscountAmount() : 0.0;
    }

    public void addToCart(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        int safeQuantity = Math.max(quantity, 1);

        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(productId)) {
                int nextQty = item.getQuantity() + safeQuantity;
                int capped = limitByAvailableStock(product, nextQty);
                if (capped <= 0) {
                    return;
                }
                item.setQuantity(capped);
                return;
            }
        }

        int initial = limitByAvailableStock(product, safeQuantity);
        if (initial > 0) {
            cartItems.add(new CartItem(product, initial));
        }
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public CartItem updateQuantity(Long productId, int quantity) {
        CartItem target = cartItems.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (target == null) {
            return null;
        }

        if (quantity <= 0) {
            removeFromCart(productId);
            return null;
        }

        int capped = limitByAvailableStock(target.getProduct(), quantity);
        if (capped <= 0) {
            removeFromCart(productId);
            return null;
        }
        target.setQuantity(capped);
        return target;
    }

    public void removeFromCart(Long productId) {
        cartItems.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public void increaseQuantity(Long productId) {
        cartItems.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> {
                    int capped = limitByAvailableStock(item.getProduct(), item.getQuantity() + 1);
                    if (capped <= 0) {
                        removeFromCart(productId);
                    } else {
                        item.setQuantity(capped);
                    }
                });
    }

    public void decreaseQuantity(Long productId) {
        cartItems.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> {
                    int next = item.getQuantity() - 1;
                    if (next <= 0) {
                        removeFromCart(productId);
                    } else {
                        item.setQuantity(next);
                    }
                });
    }

    public int getTotalItems() {
        return cartItems.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public double getSubtotal() {
        return cartItems.stream().mapToDouble(CartItem::getLineTotal).sum();
    }

    /**
     * Returns the subtotal rounded down to VND (no decimals) for fee/point rules.
     */
    public long getSubtotalVnd() {
        return (long) Math.floor(getSubtotal());
    }

    /**
     * Shipping is free when order value exceeds 1,000,000 VND and total quantity equals 2.
     * Otherwise, charge 30,000 VND plus 1 VND = 30,001 VND.
     */
    public long calculateShippingFee() {
        long subtotalVnd = getSubtotalVnd();
        int totalItems = getTotalItems();
        return (subtotalVnd > 1_000_000L && totalItems >= 2) ? 0L : 30_001L;
    }

    /**
     * Reward points: 1 point per 10,000 VND, truncated.
     * Example: 464,564 VND -> 46 points.
     */
    public long calculateRewardPoints() {
        long subtotalVnd = getSubtotalVnd();
        return subtotalVnd / 10_000L;
    }

    public void clearCart() {
        cartItems.clear();
        appliedVoucher = null;
    }

    private int limitByAvailableStock(Product product, int requested) {
        Integer promoStock = product.getPromoStock();
        if (promoStock == null) {
            return requested;
        }
        int available = Math.max(promoStock, 0);
        return Math.min(requested, available);
    }
}
