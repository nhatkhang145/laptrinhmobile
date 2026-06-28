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
	// Lay tat ca mon trong 1 hoa don
	List<OrderDetail> findByOrderId(Integer orderId);

	// Lay mon theo trang thai trong 1 hoa don
	List<OrderDetail> findByOrderIdAndStatus(Integer orderId, Integer status);

	// Tinh tong tien thuc thu: khong cong mon da huy (status = 2)
	@Query("SELECT COALESCE(SUM(od.quantity * od.priceAtSale), 0) "
			+ "FROM OrderDetail od WHERE od.order.id = :orderId AND od.status != 2")
	BigDecimal calculateTotalAmount(@Param("orderId") Integer orderId);

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

	@Query("""
			SELECT od
			FROM OrderDetail od
			WHERE od.order.table.area.restaurant.id = :resId
			AND od.status = 2
			AND od.order.createdAt BETWEEN :startDate AND :endDate
			ORDER BY od.updatedAt DESC, od.order.createdAt DESC
			""")
	List<OrderDetail> findCancelledItems(
			@Param("resId") Integer resId,
			@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);
}
