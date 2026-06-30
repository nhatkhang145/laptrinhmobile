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

/**
 * ============================================================
 *  ORDER REPOSITORY - THAO TÁC CSDL BẢNG orders
 * ============================================================
 *
 * Kế thừa JpaRepository<Order, Integer>:
 *  - Tự động có các method: findById, save, findAll, deleteById...
 *  - Hỗ trợ viết thêm các method truy vấn tùy chỉnh theo tên (Derived Query)
 *    hoặc dùng @Query với JPQL / native SQL.
 *
 * Quy tắc đặt tên Derived Query của Spring JPA:
 *   findBy[TênField][Điềukiện]  →  Spring tự tạo câu SQL tương ứng
 *   Ví dụ: findByTableIdAndStatus → SELECT * FROM orders WHERE table_id=? AND status=?
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    /**
     * TÌM HÓA ĐƠN ĐANG PHỤC VỤ CỦA 1 BÀN
     *
     * Spring JPA tự sinh SQL:
     *   SELECT * FROM orders WHERE table_id = ? AND status = ?
     *
     * Dùng khi: Nhân viên bấm vào bàn đỏ → lấy hóa đơn đang mở của bàn đó
     *
     * @param tableId ID bàn cần tìm
     * @param status  Trạng thái hóa đơn (thường truyền 0 = đang phục vụ)
     * @return Optional<Order> - có thể rỗng nếu bàn không có hóa đơn active
     */
    Optional<Order> findByTableIdAndStatus(Integer tableId, Integer status);

    /**
     * LẤY LỊCH SỬ HÓA ĐƠN CỦA 1 BÀN (TẤT CẢ TRẠNG THÁI)
     *
     * Spring JPA tự sinh SQL:
     *   SELECT * FROM orders WHERE table_id = ?
     *
     * @param tableId ID bàn cần lấy lịch sử
     * @return Danh sách tất cả hóa đơn của bàn đó
     */
    List<Order> findByTableId(Integer tableId);

    /**
     * TÌM TẤT CẢ HÓA ĐƠN ĐANG PHỤC VỤ CỦA TOÀN NHÀ HÀNG
     *
     * JPQL (Java Persistence Query Language - tên Entity, không phải tên bảng):
     *   SELECT o FROM Order o
     *   WHERE o.table.area.restaurant.id = :resId   ← Duyệt qua quan hệ: Order → Table → Area → Restaurant
     *   AND o.status = :status
     *
     * Tương đương SQL:
     *   SELECT o.* FROM orders o
     *     JOIN tables t ON o.table_id = t.id
     *     JOIN areas a ON t.area_id = a.id
     *   WHERE a.restaurant_id = ? AND o.status = ?
     *
     * Dùng khi: Màn hình "Danh sách order đang phục vụ" - hiển thị tất cả bàn đang bận
     *
     * @param resId  ID nhà hàng
     * @param status Trạng thái cần lọc (0 = đang phục vụ)
     */
    @Query("SELECT o FROM Order o WHERE o.table.area.restaurant.id = :resId AND o.status = :status")
    List<Order> findByRestaurantIdAndStatus(
            @Param("resId") Integer resId,
            @Param("status") Integer status);

    /**
     * LẤY HÓA ĐƠN ĐÃ THANH TOÁN CÓ LỌC THEO NGÀY
     *
     * JPQL:
     *   SELECT o FROM Order o
     *   WHERE o.table.area.restaurant.id = :resId
     *   AND o.status = :status                           ← Thường truyền 1 (đã thanh toán)
     *   AND (:fromDate IS NULL OR COALESCE(o.checkoutAt, o.createdAt) >= :fromDate)
     *   AND (:toDate IS NULL OR COALESCE(o.checkoutAt, o.createdAt) <= :toDate)
     *
     * Giải thích:
     *   - COALESCE(o.checkoutAt, o.createdAt): Dùng checkoutAt nếu có, nếu không thì dùng createdAt
     *   - :fromDate IS NULL: Nếu không truyền fromDate thì bỏ qua điều kiện lọc từ ngày
     *   - :toDate IS NULL: Tương tự cho toDate
     *
     * @param resId    ID nhà hàng
     * @param status   Trạng thái (1 = đã thanh toán)
     * @param fromDate Lọc từ ngày (null = không lọc)
     * @param toDate   Lọc đến ngày (null = không lọc)
     */
    @Query("SELECT o FROM Order o WHERE o.table.area.restaurant.id = :resId AND o.status = :status " +
            "AND (:fromDate IS NULL OR COALESCE(o.checkoutAt, o.createdAt) >= :fromDate) " +
            "AND (:toDate IS NULL OR COALESCE(o.checkoutAt, o.createdAt) <= :toDate)")
    List<Order> findPaidOrdersWithFilter(
            @Param("resId") Integer resId,
            @Param("status") Integer status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    /**
     * TÍNH TỔNG DOANH THU TRONG KỲ
     *
     * JPQL (dạng text block - Java 13+):
     *   SELECT COALESCE(SUM(o.totalAmount), 0)   ← Tổng tiền, trả 0 nếu không có đơn nào
     *   FROM Order o
     *   WHERE o.table.area.restaurant.id = :resId
     *   AND o.status = 1                         ← Chỉ tính đơn đã thanh toán
     *   AND COALESCE(o.checkoutAt, o.createdAt) BETWEEN :startDate AND :endDate
     *
     * Dùng cho Dashboard - ô "Doanh thu hôm nay / tuần / tháng"
     *
     * @param resId     ID nhà hàng
     * @param startDate Đầu kỳ
     * @param endDate   Cuối kỳ
     * @return Tổng doanh thu (BigDecimal, không null nhờ COALESCE)
     */
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

    /**
     * ĐẾM SỐ HÓA ĐƠN ĐÃ THANH TOÁN TRONG KỲ
     *
     * JPQL:
     *   SELECT COUNT(o)                          ← Đếm số bản ghi
     *   FROM Order o
     *   WHERE o.table.area.restaurant.id = :resId
     *   AND o.status = 1                         ← Chỉ đếm đơn đã thanh toán
     *   AND COALESCE(o.checkoutAt, o.createdAt) BETWEEN :startDate AND :endDate
     *
     * Dùng cho Dashboard - ô "Số đơn hàng hôm nay / tuần / tháng"
     *
     * @param resId     ID nhà hàng
     * @param startDate Đầu kỳ
     * @param endDate   Cuối kỳ
     * @return Số lượng hóa đơn (Long)
     */
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
