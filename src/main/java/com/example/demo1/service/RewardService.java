package com.example.demo1.service;

import com.example.demo1.model.User;
import com.example.demo1.model.Voucher;
import com.example.demo1.repository.OrderRepository;
import com.example.demo1.repository.IUserRepository;
import com.example.demo1.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RewardService {

    private final OrderRepository orderRepository;
    private final IUserRepository userRepository;
    private final VoucherRepository voucherRepository;

    public double calculateTotalEarnedPoints(Long userId) {
        return orderRepository.sumEarnedVipPointsByUserId(userId);
    }

    public double getAvailablePoints(User user) {
        double earned = calculateTotalEarnedPoints(user.getId());
        double redeemed = user.getRedeemedVipPoints();
        return Math.max(earned - redeemed, 0);
    }

    public Voucher redeemVoucher(User user, String type) {
        double pointsNeeded = 0;
        double discount = 0;
        String desc = "";

        if ("VIP1".equals(type)) {
            pointsNeeded = 100;
            discount = 50000;
            desc = "Giảm 50k cho đơn hàng trên 500k";
        } else if ("VIP2".equals(type)) {
            pointsNeeded = 200;
            discount = 120000;
            desc = "Giảm 120k cho đơn hàng trên 1tr";
        } else if ("VIP3".equals(type)) {
            pointsNeeded = 500;
            discount = 350000;
            desc = "Giảm 350k cho đơn hàng trên 2tr";
        } else {
            throw new IllegalArgumentException("Loại voucher không hợp lệ");
        }

        double available = getAvailablePoints(user);
        if (available < pointsNeeded) {
            throw new IllegalStateException("Bạn không đủ điểm tích lũy. Bạn có " + available + " điểm, cần " + pointsNeeded + " điểm.");
        }

        // Subtract points in User model
        user.setRedeemedVipPoints(user.getRedeemedVipPoints() + pointsNeeded);
        userRepository.save(user);

        // Create Voucher
        Voucher voucher = Voucher.builder()
                .code("REWD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .description(desc)
                .pointsRequired(pointsNeeded)
                .discountAmount(discount)
                .user(user)
                .used(false)
                .build();

        return voucherRepository.save(voucher);
    }
    
    public List<Voucher> getUserVouchers(User user) {
        return voucherRepository.findByUserOrderByUsedAsc(user);
    }
    
    public List<Voucher> getUnusedUserVouchers(User user) {
        return voucherRepository.findByUserOrderByUsedAsc(user).stream()
                .filter(v -> !v.isUsed())
                .toList();
    }

    
    public List<com.example.demo1.model.Order> getOrderPointsHistory(Long userId) {
        return orderRepository.findOrdersWithPointsByUserId(userId);
    }
    
    public Voucher useVoucher(String code, User user) {
        Voucher voucher = validateVoucher(code, user);
        voucher.setUsed(true);
        return voucherRepository.save(voucher);
    }
    
    public Voucher validateVoucher(String code, User user) {
        Voucher voucher = voucherRepository.findByCodeAndUsedFalse(code)
                .orElseThrow(() -> new IllegalArgumentException("Mã voucher không hợp lệ hoặc đã sử dụng."));
        
        if (voucher.getUser() != null && !voucher.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Voucher này không thuộc về bạn.");
        }
        return voucher;
    }
}



