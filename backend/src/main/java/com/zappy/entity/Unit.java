package com.zappy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BANG: units
 * Don vi tinh rieng cua tung nha hang
 * Vi du: To, Dia, Ly, Chai, Lon, Phan
 */
@Entity
@Table(name = "units")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "res_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "unit_name", nullable = false)
    private String unitName;
}
