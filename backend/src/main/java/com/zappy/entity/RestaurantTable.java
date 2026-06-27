package com.zappy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;

/**
 * BANG: tables
 * Danh sach ban an
 * is_occupied = true  -> Co khach (hien do)
 * is_occupied = false -> Trong    (hien xanh)
 */
@Entity
@Table(name = "tables")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Ban thuoc khu vuc nao
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    @Column(name = "table_name", nullable = false)
    private String tableName;  // "Ban 01", "Ban A10"...

    // true = co khach (do), false = trong (xanh)
    @Column(name = "is_occupied", nullable = false)
    private Boolean isOccupied = false;
    
    // Trạng thái bàn: HOẠT ĐỘNG, ĐANG KHÓA
    @Column(name = "status")
    private String status = "HOẠT ĐỘNG";
    
    @Transient
    private Integer activeUserId;
}
