package com.zappy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;

/**
 * ============================================================
 *  ENTITY: RestaurantTable  ←→  BẢNG CSDL: tables
 * ============================================================
 *
 * Đại diện cho 1 bàn ăn trong nhà hàng.
 *
 * TRẠNG THÁI BÀN:
 *   isOccupied = false → Bàn TRỐNG  (hiển thị màu XANH trên app)
 *   isOccupied = true  → Bàn CÓ KHÁCH (hiển thị màu ĐỎ trên app)
 *
 * isOccupied được cập nhật tự động:
 *   - Khi mở bàn (POST /orders/open):       isOccupied = true
 *   - Khi thanh toán (POST /orders/checkout): isOccupied = false
 *
 * QUAN HỆ:
 *   RestaurantTable n---1 Area  (nhiều bàn thuộc 1 khu vực)
 *   RestaurantTable 1---n Order (1 bàn có nhiều hóa đơn theo lịch sử)
 */
@Entity
@Table(name = "tables") // Ánh xạ → bảng "tables" trong CSDL
@Data                   // Lombok: getter, setter, toString, equals, hashCode
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Tránh lỗi JSON với Hibernate Proxy
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantTable {

    // Khóa chính, tự tăng
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Khu vực mà bàn này thuộc về.
     * Ví dụ: "Khu trong", "Khu ngoài", "Sân thượng"...
     *
     * LAZY: Không tải Area ngay khi lấy RestaurantTable (tối ưu hiệu năng)
     * nullable = false: Mỗi bàn PHẢI thuộc 1 khu vực nào đó
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    /**
     * Tên bàn hiển thị trên app.
     * Ví dụ: "Bàn 01", "Bàn A10", "VIP 01"...
     */
    @Column(name = "table_name", nullable = false)
    private String tableName;

    /**
     * Cờ trạng thái bàn:
     *   false = Trống   → App hiển thị màu XANH, nhân viên có thể chọn
     *   true  = Có khách → App hiển thị màu ĐỎ, không thể mở thêm hóa đơn
     *
     * Được tự động cập nhật bởi OrderController:
     *   → Mở bàn:    table.setIsOccupied(true)
     *   → Checkout:  table.setIsOccupied(false)
     */
    @Column(name = "is_occupied", nullable = false)
    private Boolean isOccupied = false; // Mặc định khi tạo bàn mới = trống

    /**
     * Trạng thái hoạt động của bàn (không liên quan đến có khách hay không):
     *   "HOẠT ĐỘNG" = Bàn đang được sử dụng bình thường
     *   "ĐANG KHÓA"  = Bàn tạm dừng (bảo trì, vệ sinh...)
     */
    @Column(name = "status")
    private String status = "HOẠT ĐỘNG";

    // Số ghế tối đa của bàn (dùng để hiển thị thông tin cho nhân viên)
    @Column(name = "seats")
    private Integer seats;

    /**
     * ID nhân viên đang phụ trách bàn này.
     *
     * @Transient: Trường này KHÔNG được lưu vào CSDL.
     * Chỉ dùng để truyền dữ liệu tạm thời qua API (ví dụ: app hỏi "ai đang phụ trách bàn này?")
     */
    @Transient
    private Integer activeUserId;
}
