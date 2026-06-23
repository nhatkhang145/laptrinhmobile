package com.zappy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * BANG: order_details
 * Chi tiet goi mon & Ghi chu
 *
 * status = 0: Nhap (co the sua/xoa - Nhan vien lam duoc)
 * status = 1: Da gui bep (KHOA - chi Quan ly moi huy duoc)
 * status = 2: Da huy (Quan ly can thiep - giu lai de doi soat)
 *
 * price_at_sale: Chot gia tai thoi diem goi, de tranh thay doi gia sau nay
 */
@Entity
@Table(name = "order_details")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Thuoc hoa don tong nao
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Mon an khach chon
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private MenuItem menuItem;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Gia ban tai thoi diem goi (chot gia - khong doi du menu sau nay thay doi)
    @Column(name = "price_at_sale", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAtSale;

    // Ghi chu cua khach: "Khong hanh ngo, Cap do cay 7, It my..."
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // 0 = Nhap, 1 = Da gui (Khoa NV), 2 = Da huy (Chi QL)
    @Column(name = "status", nullable = false)
    private Integer status = 0;
}

