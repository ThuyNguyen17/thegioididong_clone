package com.example.demo1.controller;

import com.example.demo1.model.User;
import com.example.demo1.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.demo1.model.Order;
import com.example.demo1.service.CartService;
import com.example.demo1.service.OrderService;
import com.example.demo1.service.MomoService;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.Principal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/order")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;
    @Autowired
    private CartService cartService;
    @Autowired
    private UserService userService; // Added autowired UserService
    @Autowired
    private MomoService momoService;
    @Autowired
    private com.example.demo1.repository.OrderRepository orderRepository;
    @Autowired
    private com.example.demo1.service.RewardService rewardService;

    @GetMapping("/checkout")
    public String checkout(Model model, Principal principal) { // Added Principal principal
        List<CartItem> cartItems = cartService.getCartItems();
        double subtotal = cartService.getSubtotal();
        long shippingFee = cartService.calculateShippingFee();
        long rewardPoints = cartService.calculateRewardPoints();
        double voucherDiscount = cartService.getVoucherDiscount();
        double totalAmount = Math.max(0, subtotal + shippingFee - voucherDiscount);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotalFormatted", formatCurrency(subtotal));
        model.addAttribute("shippingFeeFormatted", formatCurrency(shippingFee));
        model.addAttribute("rewardPoints", rewardPoints);
        model.addAttribute("voucherDiscount", voucherDiscount);
        model.addAttribute("appliedVoucher", cartService.getAppliedVoucher());
        model.addAttribute("totalAmountFormatted", formatCurrency(totalAmount));


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String username = auth.getName();
            User user = userService.findByUsername(username).orElse(null);
            model.addAttribute("user", user);
        }

        return "/cart/checkout";
    }

    @PostMapping("/submit")
    public String submitOrder(
            @RequestParam String customerName,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam String address,
            @RequestParam(required = false) String note,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String voucherCode,
            RedirectAttributes redirectAttributes) {

        try {
            List<CartItem> cartItems = cartService.getCartItems();
            if (cartItems.isEmpty()) {
                return "redirect:/cart";
            }

            String normalizedName = customerName == null ? "" : customerName.trim();
            if (normalizedName.isEmpty() || phone.isEmpty() || email.isEmpty() || address.isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "Vui lòng nhập đầy đủ thông tin: Tên, Số điện thoại, Email và Địa chỉ.");
                return "redirect:/order/checkout";
            }

            double subtotal = cartService.getSubtotal();
            long shippingFee = cartService.calculateShippingFee();
            double totalAmount = subtotal + shippingFee;
            long rewardPoints = cartService.calculateRewardPoints();

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = null;
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                user = userService.findByUsername(auth.getName()).orElse(null);
            }

            double vipDiscount = 0.0;
            if (voucherCode != null && !voucherCode.trim().isEmpty() && user != null) {
                try {
                    com.example.demo1.model.Voucher voucher = rewardService.useVoucher(voucherCode.trim(), user);
                    vipDiscount = voucher.getDiscountAmount();
                    totalAmount = Math.max(0, totalAmount - vipDiscount);
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("message", "Lỗi voucher: " + e.getMessage());
                    return "redirect:/order/checkout";
                }
            }

            Order order = orderService.createOrder(
                    normalizedName,
                    phone,
                    email,
                    address,
                    note,
                    paymentMethod,
                    totalAmount,
                    (double) rewardPoints,
                    vipDiscount,
                    cartItems,
                    user
            );


            if ("momo".equalsIgnoreCase(paymentMethod)) {
                try {
                    String payUrl = momoService.createPaymentUrl(order.getId(), (long) totalAmount, "Thanh toan don hang " + order.getId());
                    return "redirect:" + payUrl;
                } catch (Exception e) {
                    logger.error("MoMo connection error: ", e);
                    redirectAttributes.addFlashAttribute("message", "Lỗi khi kết nối với MoMo: " + e.getMessage());
                    return "redirect:/order/checkout";
                }
            }

            redirectAttributes.addFlashAttribute("customerName", normalizedName);
            redirectAttributes.addFlashAttribute("orderId", order.getId());
            redirectAttributes.addFlashAttribute("orderTotalFormatted", formatCurrency(totalAmount));
            redirectAttributes.addFlashAttribute("message", "Đặt hàng thành công.");

            return "redirect:/order/confirmation";
        } catch (Exception e) {
            logger.error("Order submission error: ", e);
            throw e; // Still throw to hit the error page, but now we have the log trace
        }
    }

    @GetMapping("/momo-return")
    public String momoReturn(
            @RequestParam(name = "resultCode") int resultCode,
            @RequestParam(name = "orderId") String momoOrderId,
            @RequestParam(name = "message") String momoMessage,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // MoMo orderId format: orderId_randomString
        String originalOrderIdStr = momoOrderId.split("_")[0];
        Long orderId = Long.parseLong(originalOrderIdStr);
        Order order = orderRepository.findById(orderId).orElse(null);

        if (resultCode == 0) {
            if (order != null) {
                order.setStatus("PAID");
                orderRepository.save(order);
                
                redirectAttributes.addFlashAttribute("message", "Thanh toán MoMo thành công!");
                redirectAttributes.addFlashAttribute("orderId", orderId);
                redirectAttributes.addFlashAttribute("orderTotalFormatted", formatCurrency(order.getTotalAmount()));
            }
            return "redirect:/order/confirmation";
        } else {
            if (order != null) {
                order.setStatus("PAYMENT_FAILED");
                orderRepository.save(order);
            }
            redirectAttributes.addFlashAttribute("message", "Thanh toán MoMo thất bại: " + momoMessage);
            return "redirect:/order/checkout";
        }
    }

    @PostMapping("/momo-notify")
    public void momoNotify(@RequestParam Map<String, String> params) {
        // Implement IPN logic here (verification of signature, updating status)
        // This is called by MoMo server
        System.out.println("MoMo IPN Received: " + params);
    }

    @GetMapping("/confirmation")
    public String orderConfirmation(Model model) {
        if (!model.containsAttribute("message")) {
            model.addAttribute("message", "Dat hang thanh cong.");
        }
        return "cart/order-confirmation";
    }

    private String formatCurrency(double amount) {
        DecimalFormat df = new DecimalFormat("#,##0");
        return df.format(amount) + "d";
    }
}
