package com.zappy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;

/**
 * BANG: users
 * Tai khoan & Phan quyen
 * role = 1: Quan ly (Toan quyen)
 * role = 0: Nhan vien (Han che)
 */
@Entity
@Table(name = "users")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Nhan vien thuoc nha hang nao
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "res_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "username", nullable = false)
    private String username;

    // Mat khau da duoc hash (BCrypt)
    @Column(name = "password", nullable = false)
    private String password;

    // 1 = Quan ly, 0 = Nhan vien
    @Column(name = "role", nullable = false)
    private Integer role = 0;
    // Email cua nguoi dung
    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "fullname")
    private String fullname;
}

