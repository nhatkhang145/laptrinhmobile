package com.zappy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * BANG: menu_items
 * San pham mon an
 * Vi du: My cay bo, My hai san, Tra dao
 */
@Entity
@Table(name = "menu_items")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Thuoc danh muc nao
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cat_id", nullable = false)
    private Category category;

    // Dung don vi tinh nao
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    // Gia ban niem yet
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
}

