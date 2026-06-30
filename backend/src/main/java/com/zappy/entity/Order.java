package com.zappy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ============================================================
 *  ENTITY: Order  ←→  BẢNG CSDL: orders
 * ============================================================
 *
 * Đây là hóa đơn tổng cho 1 lần phục vụ của 1 bàn.
 * Mỗi lần khách ngồi vào bàn → tạo 1 Order mới.
 *
 * TRẠNG THÁI (status):
 *   status = 0 → Đang phục vụ  (bàn màu đỏ trên app)
 *   status = 1 → Đã thanh toán (lưu lại trong lịch sử)
 *   status = 2 → Đã hủy        (hiếm dùng)
 *
 * QUAN HỆ:
 *   Order n---1 RestaurantTable  (nhiều hóa đơn thuộc 1 bàn - theo lịch sử)
 *   Order n---1 User             (nhiều hóa đơn do 1 nhân viên tạo)
 *   Order 1---n OrderDetail      (1 hóa đơn có nhiều dòng món)
 */
@Entity
@Table(name = "orders") // Ánh xạ class này → bảng "orders" trong CSDL
@Data                   // Lombok: tự sinh getter, setter, toString, equals, hashCode
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Tránh lỗi JSON khi Lazy Load
@NoArgsConstructor      // Lombok: tự sinh constructor không tham số (bắt buộc với JPA)
@AllArgsConstructor     // Lombok: tự sinh constructor đầy đủ tham số
public class Order {

    // Khóa chính, tự tăng (AUTO INCREMENT trong MySQL)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Bàn nào đang được phục vụ trong hóa đơn này.
     *
     * @ManyToOne: Nhiều hóa đơn có thể thuộc 1 bàn (theo lịch sử thời gian)
     * FetchType.LAZY: Không load dữ liệu bàn ngay, chỉ load khi cần → tối ưu hiệu năng
     * @JoinColumn(name = "table_id"): Cột khóa ngoại trong bảng orders trỏ đến bảng tables
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable table;

    /**
     * Trạng thái hóa đơn:
     *   0 = Đang phục vụ  → Bàn còn đang có khách
     *   1 = Đã thanh toán → Bàn đã được giải phóng
     *   2 = Đã hủy        → Trường hợp đặc biệt
     */
    @Column(name = "status", nullable = false)
    private Integer status = 0; // Mặc định khi tạo mới = 0 (đang phục vụ)

    /**
     * Tổng tiền thực thu sau khi thanh toán.
     * Được tính lại ở bước checkout: SUM(quantity * priceAtSale) của các món chưa hủy.
     * Trong khi phục vụ: trường này chưa được cập nhật (= 0), phải tính tạm từ order_details.
     *
     * precision=12, scale=2: Tối đa 12 chữ số, 2 chữ số thập phân (ví dụ: 9,999,999,999.99)
     */
    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /**
     * Nhân viên phụ trách hóa đơn này (người đã bấm "Mở bàn").
     * Dùng để theo dõi hiệu suất nhân viên và phân quyền.
     *
     * nullable: có thể null nếu không gán nhân viên
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Thời điểm tạo hóa đơn (khi nhân viên mở bàn).
     * Được tự động điền bởi @PrePersist → không cần set thủ công.
     */
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    /**
     * Thời điểm thanh toán (checkout).
     * Chỉ có giá trị sau khi bước checkout hoàn tất, ban đầu = null.
     * Dùng để tính doanh thu theo ngày/tuần/tháng trong báo cáo.
     */
    @Column(name = "checkout_at")
    private java.time.LocalDateTime checkoutAt;

    /**
     * Callback JPA: Tự động chạy TRƯỚC KHI lưu bản ghi mới vào CSDL.
     * Mục đích: Tự động điền createdAt = thời điểm hiện tại.
     * → Không cần set createdAt trong Controller.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }
}
