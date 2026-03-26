package com.example.demo1.model;

import jakarta.persistence.*;
import lombok.*;

import java.text.DecimalFormat;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double price;
    private String description;
    private String imageUrl;
        private Double rating;
    private Integer discount;
    private String brand;
    private String specs;
    private String code;
    private Boolean upcoming;
    private Double installmentPrice;
    private Double discountedPrice;
    /**
     * Số lượng tồn kho hiện tại. Giảm đi 1 mỗi khi có đơn hàng.
     */
    @Column(name = "promo_stock")
    private Integer promoStock;
    /**
     * Tổng số lượng ban đầu. Không thay đổi khi bán.
     */
    @Column(name = "promo_total")
    private Integer promoTotal;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    public String getPriceFormatted() {
        DecimalFormat df = new DecimalFormat("#,##0");
        return df.format(price) + "đ";
    }

    public String getInstallmentPriceFormatted() {
        if (installmentPrice == null) {
            return null;
        }
        DecimalFormat df = new DecimalFormat("#,##0");
        return df.format(installmentPrice);
    }

    public boolean isDiscounted() {
        return discountedPrice != null && discountedPrice > 0 && discountedPrice < price;
    }

    public boolean isPromoAvailable() {
        return isDiscounted() && promoStock != null && promoStock > 0;
    }

    public boolean isInStock() {
        return promoStock == null || promoStock > 0;
    }

    public String getDiscountedPriceFormatted() {
        if (discountedPrice == null) {
            return null;
        }
        DecimalFormat df = new DecimalFormat("#,##0");
        return df.format(discountedPrice) + "đ";
    }
}
