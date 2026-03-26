package com.example.demo1.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private String address;
    private String phone;
    private String email;
    private String note;
    private String paymentMethod;
    private double totalAmount;
    private String status; // New field
    private double earnedVipPoints;
    private double vipDiscount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;
}