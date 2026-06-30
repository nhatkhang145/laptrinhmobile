package com.zappy.repository;

import com.zappy.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================
 *  ORDER DETAIL REPOSITORY - THAO TÁC CSDL BẢNG order_details
 * ============================================================
 *
 * Mỗi bản ghi OrderDetail = 1 dòng món ăn trong hóa đơn.
 * Ví dụ: Hóa đơn #5 có 3 món → có 3 bản ghi trong order_details.
 *
 * Các trạng thái (status) của món:
 *   status = 0 → Nhập (chưa gửi bếp)   - Nhân viên có thể xóa
 *   status = 1 → Đã gửi bếp (KHÓA)     - Chỉ Quản lý mới hủy được
 *   status = 2 → Đã hủy                 - Giữ lại để đối soát
 */
@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    /**
     * LẤY TẤT CẢ MÓN TRONG 1 HÓA ĐƠN (MỌI TRẠNG THÁI)
     *
     * Spring JPA tự sinh SQL:
     *   SELECT * FROM order_details WHERE order_id = ?
     *
     * Trả về cả 3 loại: nhập, đã gửi, đã hủy.
     * App Android dùng để hiển thị danh sách món với màu sắc khác nhau.
     *
     * @param orderId ID hóa đơn
     * @return Danh sách tất cả món trong hóa đơn
     */
    List<OrderDetail> findByOrderId(Integer orderId);

    /**
     * LẤY MÓN THEO TRẠNG THÁI TRONG 1 HÓA ĐƠN
     *
     * Spring JPA tự sinh SQL:
     *   SELECT * FROM order_details WHERE order_id = ? AND status = ?
     *
     * Dùng cho:
     *   - Lấy món chưa gửi (status=0) → để xóa hoặc gửi bếp
     *   - Lấy món đã gửi (status=1)   → để tính tiền hoặc hủy
     *
     * @param orderId ID hóa đơn
     * @param status  Trạng thái cần lọc (0 / 1 / 2)
     * @return Danh sách món theo trạng thái
     */
    List<OrderDetail> findByOrderIdAndStatus(Integer orderId, Integer status);

    /**
     * TÍNH TỔNG TIỀN THỰC THU CỦA 1 HÓA ĐƠN
     *
     * JPQL:
     *   SELECT COALESCE(SUM(od.quantity * od.priceAtSale), 0)
     *   FROM OrderDetail od
     *   WHERE od.order.id = :orderId
     *   AND od.status != 2                    ← KHÔNG tính món đã hủy (status = 2)
     *
     * Giải thích:
     *   - quantity * priceAtSale: Thành tiền của từng dòng món
     *   - SUM: Tổng cộng tất cả dòng
     *   - COALESCE(..., 0): Trả về 0 thay vì NULL nếu hóa đơn chưa có món nào
     *   - status != 2: Loại trừ món đã hủy khỏi tổng tiền
     *
     * Dùng khi:
     *   - Hiển thị tổng tiền tạm tính trong khi phục vụ
     *   - Lúc checkout để tính số tiền khách phải trả
     *
     * @param orderId ID hóa đơn
     * @return Tổng tiền thực thu (BigDecimal, không bao giờ null)
     */
    @Query("SELECT COALESCE(SUM(od.quantity * od.priceAtSale), 0) "
            + "FROM OrderDetail od WHERE od.order.id = :orderId AND od.status != 2")
    BigDecimal calculateTotalAmount(@Param("orderId") Integer orderId);

    /**
     * ĐẾM TỔNG SỐ LƯỢNG MÓN ĐÃ HỦY TRONG KỲ
     *
     * JPQL:
     *   SELECT COALESCE(SUM(od.quantity), 0)
     *   FROM OrderDetail od
     *   WHERE od.order.table.area.restaurant.id = :resId   ← Lọc theo nhà hàng (duyệt chuỗi quan hệ)
     *   AND od.status = 2                                  ← Chỉ tính món đã hủy
     *   AND od.order.createdAt BETWEEN :startDate AND :endDate
     *
     * Tương đương SQL:
     *   SELECT COALESCE(SUM(od.quantity), 0)
     *   FROM order_details od
     *     JOIN orders o ON od.order_id = o.id
     *     JOIN tables t ON o.table_id = t.id
     *     JOIN areas a ON t.area_id = a.id
     *   WHERE a.restaurant_id = ? AND od.status = 2
     *   AND o.created_at BETWEEN ? AND ?
     *
     * Dùng cho Dashboard - ô "Số món hủy hôm nay / tuần / tháng"
     *
     * @param resId     ID nhà hàng
     * @param startDate Đầu kỳ
     * @param endDate   Cuối kỳ
     * @return Tổng số lượng món đã hủy (Long)
     */
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

    /**
     * LẤY DANH SÁCH CHI TIẾT CÁC MÓN ĐÃ HỦY TRONG KỲ
     *
     * JPQL:
     *   SELECT od
     *   FROM OrderDetail od
     *   WHERE od.order.table.area.restaurant.id = :resId
     *   AND od.status = 2                              ← Chỉ lấy món đã hủy
     *   AND COALESCE(od.updatedAt, CURRENT_TIMESTAMP) BETWEEN :startDate AND :endDate
     *   ORDER BY COALESCE(od.updatedAt, CURRENT_TIMESTAMP) DESC   ← Mới nhất lên trên
     *
     * Giải thích:
     *   - Lọc theo updatedAt (thời điểm hủy), nếu null thì dùng thời điểm hiện tại
     *   - ORDER BY DESC: Sắp xếp món hủy gần nhất lên đầu danh sách
     *
     * Dùng cho: Màn hình "Báo cáo hủy món" của quản lý
     * (Xem ai hủy món gì, lúc nào, lý do gì)
     *
     * @param resId     ID nhà hàng
     * @param startDate Đầu kỳ lọc
     * @param endDate   Cuối kỳ lọc
     * @return Danh sách OrderDetail đã hủy, sắp xếp mới nhất lên trên
     */
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
