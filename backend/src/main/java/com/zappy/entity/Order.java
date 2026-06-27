package com.zappy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * BANG: orders
 * Hoa don tong
 * status = 0: Dang phuc vu
 * status = 1: Da thanh toan
 * status = 2: Da huy
 */
@Entity
@Table(name = "orders")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Gan voi ban cu the
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable table;

    // 0 = Phuc vu, 1 = Da thanh toan, 2 = Da huy
    @Column(name = "status", nullable = false)
    private Integer status = 0;

    // Tong tien thuc thu (chi cong nhung mon status = 1)
    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    // Nhan vien tao order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }
}
