package com.example.demo1.controller;

import com.example.demo1.service.CartService;
import com.example.demo1.service.RewardService;
import com.example.demo1.service.UserService;
import com.example.demo1.model.User;
import com.example.demo1.model.Voucher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private RewardService rewardService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String showCart(Model model) {
        double cartSubtotal = cartService.getSubtotal();
        int cartTotalItems = cartService.getTotalItems();
        long shippingFee = cartService.calculateShippingFee();
        long rewardPoints = cartService.calculateRewardPoints();
        double voucherDiscount = cartService.getVoucherDiscount();
        double cartTotal = Math.max(0, cartSubtotal + shippingFee - voucherDiscount);

        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("cartSubtotal", cartSubtotal);
        model.addAttribute("cartTotalItems", cartTotalItems);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("voucherDiscount", voucherDiscount);
        model.addAttribute("appliedVoucher", cartService.getAppliedVoucher());
        model.addAttribute("cartTotal", cartTotal);
        model.addAttribute("rewardPoints", rewardPoints);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            User user = userService.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("availableVouchers", rewardService.getUnusedUserVouchers(user));
            }
        }

        return "cart/cart";
    }

    @PostMapping("/quantity")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateQuantity(@RequestParam Long productId,
                                                              @RequestParam int quantity) {
        CartItem updatedItem = cartService.updateQuantity(productId, quantity);

        double cartSubtotal = cartService.getSubtotal();
        int cartTotalItems = cartService.getTotalItems();
        long shippingFee = cartService.calculateShippingFee();
        long rewardPoints = cartService.calculateRewardPoints();
        double voucherDiscount = cartService.getVoucherDiscount();
        double cartTotal = Math.max(0, cartSubtotal + shippingFee - voucherDiscount);

        Map<String, Object> body = new HashMap<>();
        body.put("removed", updatedItem == null);
        body.put("quantity", updatedItem == null ? 0 : updatedItem.getQuantity());
        body.put("lineTotalFormatted", updatedItem == null ? "0đ" : updatedItem.getLineTotalFormatted());
        body.put("cartSubtotalFormatted", formatCurrency(cartSubtotal));
        body.put("cartTotalItems", cartTotalItems);
        body.put("shippingFeeFormatted", formatCurrency(shippingFee));
        body.put("voucherDiscountFormatted", formatCurrency(voucherDiscount));
        body.put("cartTotalFormatted", formatCurrency(cartTotal));
        body.put("rewardPoints", rewardPoints);

        return ResponseEntity.ok(body);
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId, @RequestParam int quantity) {
        cartService.addToCart(productId, quantity);
        return "redirect:/cart";
    }

    @GetMapping("/add")
    public String addToCartGet() {
        return "redirect:/cart";
    }

    @GetMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId) {
        cartService.removeFromCart(productId);
        return "redirect:/cart";
    }

    @GetMapping("/increase/{productId}")
    public String increaseQuantity(@PathVariable Long productId) {
        cartService.increaseQuantity(productId);
        return "redirect:/cart";
    }

    @GetMapping("/decrease/{productId}")
    public String decreaseQuantity(@PathVariable Long productId) {
        cartService.decreaseQuantity(productId);
        return "redirect:/cart";
    }

    @GetMapping("/clear")
    public String clearCart() {
        cartService.clearCart();
        return "redirect:/cart";
    }

    @PostMapping("/apply-voucher")
    public String applyVoucher(@RequestParam String voucherCode, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để áp dụng voucher.");
            return "redirect:/cart";
        }

        User user = userService.findByUsername(auth.getName())
                .orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng.");
            return "redirect:/cart";
        }

        try {
            // Check rewardService for an UNUSED voucher by code (will implement properly in rewardService if needed)
            // But let's build it here if RewardService doesn't have a direct "find" yet.
            // Wait, RewardService.useVoucher(...) marks it as used.
            // I should have a "validateVoucher" instead of "useVoucher" because they can still cancel.
            // But for simplicity, I'll allow applying it here.
            
            // Re-using rewardService check
            Voucher voucher = rewardService.validateVoucher(voucherCode, user);
            cartService.setAppliedVoucher(voucher);
            redirectAttributes.addFlashAttribute("successMessage", "Áp dụng voucher thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }

    @GetMapping("/remove-voucher")
    public String removeVoucher() {
        // Need to mark the voucher as unused again if we already called useVoucher?
        // This is tricky. A better system is "Reservation".
        // But for now, if they remove it, we'll just clear it from session.
        cartService.setAppliedVoucher(null);
        return "redirect:/cart";
    }

    private String formatCurrency(double amount) {
        DecimalFormat df = new DecimalFormat("#,##0");
        return df.format(amount) + "đ";
    }
}
