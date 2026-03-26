package com.example.demo1.controller;

import com.example.demo1.model.Product;

import java.io.Serializable;
import java.text.DecimalFormat;

public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getDiscountedQuantity() {
        if (product == null || !product.isPromoAvailable()) {
            return 0;
        }
        int promo = Math.max(product.getPromoStock() == null ? 0 : product.getPromoStock(), 0);
        return Math.min(quantity, promo);
    }

    public int getRegularQuantity() {
        return Math.max(0, quantity - getDiscountedQuantity());
    }

    /**
     * Effective unit price shown in some UI places. For mixed pricing it is an average.
     */
    public double getUnitPrice() {
        return quantity > 0 ? (getLineTotal() / quantity) : 0.0;
    }

    public double getLineTotal() {
        if (product == null || quantity <= 0) {
            return 0.0;
        }

        int discountedQty = getDiscountedQuantity();
        int regularQty = Math.max(0, quantity - discountedQty);

        double total = 0.0;
        if (discountedQty > 0 && product.getDiscountedPrice() != null) {
            total += product.getDiscountedPrice() * discountedQty;
        }
        if (regularQty > 0) {
            total += product.getPrice() * regularQty;
        }
        return total;
    }

    public String getLineTotalFormatted() {
        return formatCurrency(getLineTotal());
    }

    /**
     * Returns either one unit price or a breakdown when mixed:
     * "20 x 9000d + 1 x 10000d"
     */
    public String getUnitPriceFormatted() {
        if (product == null) {
            return formatCurrency(0);
        }

        if (getDiscountedQuantity() > 0 && product.getDiscountedPrice() != null) {
            return formatCurrency(product.getDiscountedPrice());
        }
        return formatCurrency(product.getPrice());
    }

    private static String formatCurrency(double amount) {
        DecimalFormat df = new DecimalFormat("#,##0");
        return df.format(amount) + "d";
    }
}

