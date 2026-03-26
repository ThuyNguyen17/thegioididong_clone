package com.example.demo1.repository;

import com.example.demo1.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(o.earnedVipPoints), 0) FROM Order o WHERE o.user.id = :userId AND (o.status = 'PAID' OR o.status = 'PROCESSING')")
    double sumEarnedVipPointsByUserId(@Param("userId") Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.earnedVipPoints > 0 ORDER BY o.id DESC")
    java.util.List<Order> findOrdersWithPointsByUserId(@Param("userId") Long userId);
}