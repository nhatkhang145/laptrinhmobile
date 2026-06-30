package com.zappy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ============================================================
 *  ENTITY: OrderDetail  ←→  BẢNG CSDL: order_details
 * ============================================================
 *
 * Mỗi bản ghi = 1 dòng món ăn trong hóa đơn.
 * Ví dụ: Hóa đơn #5 gọi "Mì cay bò x2 + Trà đào x1" → 2 bản ghi OrderDetail.
 *
 * TRẠNG THÁI (status) - Quy trình 3 bước:
 * ┌────────────────────────────────────────────────────────┐
 * │  status = 0  →  NHẬP                                  │
 * │  Nhân viên vừa thêm vào giỏ, chưa gửi bếp.            │
 * │  → Nhân viên CÓ THỂ xóa                               │
 * ├────────────────────────────────────────────────────────┤
 * │  status = 1  →  ĐÃ GỬI BẾP (KHÓA)                    │
 * │  Nhân viên bấm "Gửi bếp", bếp đã nhận.                │
 * │  → Nhân viên KHÔNG THỂ xóa                            │
 * │  → Chỉ Quản lý mới hủy được (chuyển sang status = 2)  │
 * ├────────────────────────────────────────────────────────┤
 * │  status = 2  →  ĐÃ HỦY                                │
 * │  Quản lý đã hủy. Vẫn GIỮ LẠI trong DB để đối soát.   │
 * │  Không tính vào tổng tiền khi checkout.                │
 * └────────────────────────────────────────────────────────┘
 *
 * QUAN HỆ:
 *   OrderDetail n---1 Order    (nhiều dòng món thuộc 1 hóa đơn)
 *   OrderDetail n---1 MenuItem (nhiều dòng có thể cùng 1 món)
 */
@Entity
@Table(name = "order_details") // Ánh xạ class này → bảng "order_details" trong CSDL
@Data                          // Lombok: tự sinh getter, setter, toString, equals, hashCode
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Tránh lỗi serialize JSON với Lazy Load
@NoArgsConstructor             // Lombok: constructor không tham số (bắt buộc với JPA)
@AllArgsConstructor            // Lombok: constructor đầy đủ tham số
public class OrderDetail {

    // Khóa chính, tự tăng
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Hóa đơn tổng mà dòng món này thuộc về.
     *
     * @ManyToOne: Nhiều dòng món → cùng 1 hóa đơn
     * LAZY: Không tải toàn bộ Order khi lấy OrderDetail, chỉ tải khi gọi getOrder()
     * nullable = false: Bắt buộc phải có hóa đơn (không được null)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Món ăn khách đã chọn từ menu.
     *
     * @ManyToOne: Nhiều dòng order_detail có thể cùng chọn 1 món
     * (ví dụ: nhiều bàn khác nhau cùng gọi "Mì cay bò")
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private MenuItem menuItem;

    // Số lượng khách gọi (ví dụ: 2 tô Mì cay bò)
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * Giá bán TẠI THỜI ĐIỂM GỌI MÓN - rất quan trọng!
     *
     * Lý do lưu riêng (không dùng menuItem.price trực tiếp):
     *   → Nếu quản lý chỉnh giá menu sau này, hóa đơn cũ vẫn đúng giá cũ.
     *   → Đảm bảo tính toán thanh toán chính xác, tránh tranh chấp với khách.
     *
     * Ví dụ: Lúc gọi Trà đào giá 35.000đ → priceAtSale = 35000
     *        Sau này admin đổi giá Trà đào lên 40.000đ → không ảnh hưởng hóa đơn này
     */
    @Column(name = "price_at_sale", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAtSale;

    /**
     * Ghi chú đặc biệt của khách cho món này.
     * Ví dụ: "Không hành ngò", "Cấp độ cay 7", "Ít mỡ", "Thêm rau"...
     *
     * columnDefinition = "TEXT": Cho phép ghi chú dài (không giới hạn VARCHAR 255)
     */
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    /**
     * Trạng thái của dòng món:
     *   0 = Nhập (nhân viên chưa gửi bếp)
     *   1 = Đã gửi bếp (bị khóa, chỉ QL hủy)
     *   2 = Đã hủy (QL đã can thiệp, giữ để đối soát)
     */
    @Column(name = "status", nullable = false)
    private Integer status = 0; // Mặc định khi tạo = 0 (nhập)

    /**
     * Lý do hủy món - chỉ có giá trị khi status = 2.
     * Ghi lại để đối soát, tránh gian lận nội bộ.
     * Ví dụ: "Khách đổi ý", "Hết nguyên liệu", "Nhầm bàn"...
     */
    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    /**
     * Thời điểm cập nhật lần cuối (thường là lúc hủy món).
     * Được set thủ công trong Controller khi hủy:
     *   detail.setUpdatedAt(LocalDateTime.now())
     */
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
}
