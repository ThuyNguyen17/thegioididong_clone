package com.example.demo1.repository;

import com.example.demo1.model.User;
import com.example.demo1.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    List<Voucher> findByUserOrderByUsedAsc(User user);
    java.util.Optional<Voucher> findByCodeAndUsedFalse(String code);
}

