package com.zappy.repository;

import com.zappy.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    // Lấy tất cả món trong 1 hóa đơn
    List<OrderDetail> findByOrderId(Integer orderId);

    // Lấy món theo trạng thái trong 1 hóa đơn
    List<OrderDetail> findByOrderIdAndStatus(Integer orderId, Integer status);

    // Tính tổng tiền thực thu của hóa đơn (không tính món đã hủy)
    @Query("SELECT COALESCE(SUM(od.quantity * od.priceAtSale), 0) "
            + "FROM OrderDetail od WHERE od.order.id = :orderId AND od.status != 2")
    BigDecimal calculateTotalAmount(@Param("orderId") Integer orderId);

    // Đếm tổng số lượng món đã hủy
    @Query("""
            SELECT COALESCE(SUM(od.quantity),0)
            FROM OrderDetail od
            WHERE od.order.table.area.restaurant.id = :resId
            AND od.status = 2
            AND od.order.createdAt BETWEEN :startDate AND :endDate
            """)
    Long countCancelledItems(
            @Param("resId") Integer resId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Lấy danh sách chi tiết các món đã hủy
    @Query("""
            SELECT od
            FROM OrderDetail od
            WHERE od.order.table.area.restaurant.id = :resId
            AND od.status = 2
            AND COALESCE(od.updatedAt, CURRENT_TIMESTAMP) BETWEEN :startDate AND :endDate
            ORDER BY COALESCE(od.updatedAt, CURRENT_TIMESTAMP) DESC
            """)
    List<OrderDetail> findCancelledItems(
            @Param("resId") Integer resId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
