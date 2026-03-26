package com.example.demo1.controller;

import com.example.demo1.model.User;
import com.example.demo1.model.Voucher;
import com.example.demo1.service.RewardService;
import com.example.demo1.service.SmsService;
import com.example.demo1.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/reward")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;
    private final UserService userService;
    private final SmsService smsService;
    private final jakarta.servlet.http.HttpSession session;

    @GetMapping
    public String showRewardPoints(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        double earnedPoints = rewardService.calculateTotalEarnedPoints(user.getId());
        double availablePoints = rewardService.getAvailablePoints(user);
        List<Voucher> vouchers = rewardService.getUserVouchers(user);

        model.addAttribute("earnedPoints", earnedPoints);
        model.addAttribute("availablePoints", availablePoints);
        model.addAttribute("redeemedPoints", user.getRedeemedVipPoints());
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("pointHistory", rewardService.getOrderPointsHistory(user.getId()));

        return "rewards/reward-center";
    }


    @PostMapping("/send-otp")
    @ResponseBody
    public java.util.Map<String, Object> sendOtp(@RequestParam String type) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            response.put("success", false);
            response.put("message", "Bạn cần đăng nhập.");
            return response;
        }

        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (user.getPhone() == null || user.getPhone().isEmpty()) {
            response.put("success", false);
            response.put("message", "Tài khoản của bạn chưa có số điện thoại để xác thực.");
            return response;
        }

        // Optional: Check if user has enough points before sending OTP
        double pointsNeeded = switch (type) {
            case "VIP1" -> 100;
            case "VIP2" -> 200;
            case "VIP3" -> 500;
            default -> -1;
        };

        if (pointsNeeded == -1) {
            response.put("success", false);
            response.put("message", "Loại voucher không hợp lệ.");
            return response;
        }

        if (rewardService.getAvailablePoints(user) < pointsNeeded) {
            response.put("success", false);
            response.put("message", "Bạn không đủ điểm tích lũy để đổi voucher này.");
            return response;
        }

        String otp = smsService.generateOtp();
        session.setAttribute("REDEEM_OTP", otp);
        session.setAttribute("REDEEM_TYPE", type);
        
        smsService.sendOtp(user.getPhone(), otp);
        
        response.put("success", true);
        response.put("phone", user.getPhone().replaceAll("(\\d{3})\\d+(\\d{3})", "$1****$2"));
        return response;
    }

    @PostMapping("/redeem")
    public String redeemVoucher(@RequestParam String type, @RequestParam String otp, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        String sessionOtp = (String) session.getAttribute("REDEEM_OTP");
        String sessionType = (String) session.getAttribute("REDEEM_TYPE");

        if (sessionOtp == null || !sessionOtp.equals(otp) || !type.equals(sessionType)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã xác thực không chính xác hoặc đã hết hạn.");
            return "redirect:/reward";
        }

        // Clear session after verification
        session.removeAttribute("REDEEM_OTP");
        session.removeAttribute("REDEEM_TYPE");

        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        try {
            Voucher voucher = rewardService.redeemVoucher(user, type);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi voucher thành công! Mã của bạn là: " + voucher.getCode());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/reward";
    }
}
