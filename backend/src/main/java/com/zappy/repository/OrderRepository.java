package com.zappy.repository;

import com.zappy.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // Tìm hóa đơn đang phục vụ của 1 bàn
    Optional<Order> findByTableIdAndStatus(Integer tableId, Integer status);

    // Lấy lịch sử hóa đơn của 1 bàn
    List<Order> findByTableId(Integer tableId);

    // Tìm tất cả hóa đơn đang phục vụ của nhà hàng
    @Query("SELECT o FROM Order o WHERE o.table.area.restaurant.id = :resId AND o.status = :status")
    List<Order> findByRestaurantIdAndStatus(
            @Param("resId") Integer resId,
            @Param("status") Integer status);

    // Lấy ds hóa đơn đã thanh toán có lọc theo khoảng thời gian
    @Query("SELECT o FROM Order o WHERE o.table.area.restaurant.id = :resId AND o.status = :status " +
            "AND (:fromDate IS NULL OR COALESCE(o.checkoutAt, o.createdAt) >= :fromDate) " +
            "AND (:toDate IS NULL OR COALESCE(o.checkoutAt, o.createdAt) <= :toDate)")
    List<Order> findPaidOrdersWithFilter(
            @Param("resId") Integer resId,
            @Param("status") Integer status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    // Tính tổng doanh thu theo khoảng thời gian
    @Query("""
            SELECT COALESCE(SUM(o.totalAmount),0)
            FROM Order o
            WHERE o.table.area.restaurant.id = :resId
            AND o.status = 1
            AND COALESCE(o.checkoutAt, o.createdAt) BETWEEN :startDate AND :endDate
            """)
    BigDecimal getRevenue(
            @Param("resId") Integer resId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Đếm số lượng đơn hàng theo khoảng thời gian
    @Query("""
            SELECT COUNT(o)
            FROM Order o
            WHERE o.table.area.restaurant.id = :resId
            AND o.status = 1
            AND COALESCE(o.checkoutAt, o.createdAt) BETWEEN :startDate AND :endDate
            """)
    Long countOrders(
            @Param("resId") Integer resId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
