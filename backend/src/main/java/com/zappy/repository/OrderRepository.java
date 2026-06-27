package com.zappy.repository;

import com.zappy.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    // Tim hoa don dang phuc vu (status=0) cua 1 ban
    Optional<Order> findByTableIdAndStatus(Integer tableId, Integer status);
    // Lich su hoa don cua 1 ban
    List<Order> findByTableId(Integer tableId);
    
    // Tim tat ca hoa don dang phuc vu cua 1 nha hang
    @org.springframework.data.jpa.repository.Query("SELECT o FROM Order o WHERE o.table.area.restaurant.id = :resId AND o.status = :status")
    List<Order> findByRestaurantIdAndStatus(@org.springframework.data.repository.query.Param("resId") Integer resId, @org.springframework.data.repository.query.Param("status") Integer status);

    @org.springframework.data.jpa.repository.Query("SELECT o FROM Order o WHERE o.table.area.restaurant.id = :resId AND o.status = :status " +
            "AND (:fromDate IS NULL OR o.checkoutAt >= :fromDate) AND (:toDate IS NULL OR o.checkoutAt <= :toDate)")
    List<Order> findPaidOrdersWithFilter(@org.springframework.data.repository.query.Param("resId") Integer resId, 
                                         @org.springframework.data.repository.query.Param("status") Integer status, 
                                         @org.springframework.data.repository.query.Param("fromDate") java.time.LocalDateTime fromDate, 
                                         @org.springframework.data.repository.query.Param("toDate") java.time.LocalDateTime toDate);
}
