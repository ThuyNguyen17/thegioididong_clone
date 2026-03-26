//package com.example.demo1.service;
//
//
//import com.example.demo1.controller.CartItem;
//import com.example.demo1.model.Order;
//import com.example.demo1.model.OrderDetail;
//import com.example.demo1.repository.OrderDetailRepository;
//import com.example.demo1.repository.OrderRepository;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class OrderService {
//    @Autowired
//    private OrderRepository orderRepository;
//    @Autowired
//    private OrderDetailRepository orderDetailRepository;
//    @Autowired
//    private CartService cartService;  // Assuming you have a CartService
//
//    public Order createOrder(String customerName, List<CartItem> cartItems) {
//        Order order = new Order();
//        order.setCustomerName(customerName);
//        order = orderRepository.save(order);
//
//        for (CartItem item : cartItems) {
//            OrderDetail detail = new OrderDetail();
//            detail.setOrder(order);
//            detail.setProduct(item.getProduct());
//            detail.setQuantity(item.getQuantity());
//            orderDetailRepository.save(detail);
//        }
//        // Optionally clear the cart after order placement
//        cartService.clearCart();
//
//        return order;
//    }
//}
package com.example.demo1.service;

import com.example.demo1.controller.CartItem;
import com.example.demo1.model.Order;
import com.example.demo1.model.OrderDetail;
import com.example.demo1.model.Product;
import com.example.demo1.repository.OrderDetailRepository;
import com.example.demo1.repository.OrderRepository;
import com.example.demo1.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartService cartService;
    private final ProductRepository productRepository;

    public Order createOrder(String customerName, String phone, String email, String address, String note,
                             String paymentMethod, double totalAmount, double earnedVipPoints, double vipDiscount,
                             List<CartItem> cartItems, com.example.demo1.model.User user) {
        Order order = new Order();
        order.setCustomerName(customerName);
        order.setPhone(phone);
        order.setEmail(email);
        order.setAddress(address);
        order.setNote(note);
        order.setPaymentMethod(paymentMethod);
        order.setTotalAmount(totalAmount);
        order.setEarnedVipPoints(earnedVipPoints);
        order.setVipDiscount(vipDiscount);
        order.setUser(user);
        
        if ("momo".equalsIgnoreCase(paymentMethod)) {
            order.setStatus("PENDING_PAYMENT");
        } else {
            order.setStatus("PROCESSING");
        }
        
        order = orderRepository.save(order);

        for (CartItem item : cartItems) {
            Long productId = item.getProduct() == null ? null : item.getProduct().getId();
            if (productId == null) {
                continue;
            }

            // Fetch fresh instance to ensure we're working with current DB state
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalStateException("Product not found: " + productId));

            int qty = Math.max(item.getQuantity(), 0);
            if (qty <= 0) {
                continue;
            }

            // Check promo stock
            Integer currentPromoStock = product.getPromoStock();
            if (currentPromoStock != null && currentPromoStock < qty) {
                throw new IllegalStateException("Sản phẩm '" + product.getName() + "' chỉ còn " + currentPromoStock + " sản phẩm.");
            }

            // Handle Promo Stock - giảm promoStock mỗi khi đặt hàng
            if (product.isPromoAvailable() && currentPromoStock != null) {
                // Giảm promoStock đi số lượng đặt hàng
                product.setPromoStock(currentPromoStock - qty);

                // Nếu promo stock hết thì tắt giảm giá
                if (product.getPromoStock() <= 0) {
                    product.setPromoStock(0);
                    product.setDiscountedPrice(null);
                }
            }

            // Force save and flush to ensure database is updated immediately
            productRepository.saveAndFlush(product);

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setQuantity(qty);
            orderDetailRepository.save(detail);
        }

        // Clear the session-scoped cart
        cartService.clearCart();

        return order;
    }
}
