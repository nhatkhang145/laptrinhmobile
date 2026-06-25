package com.zappy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BANG: restaurants
 * Thong tin he thong nha hang / chi nhanh
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "restaurants")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "res_name", nullable = false)
    private String resName;

    // Ten mien dang nhap - phai unique giua cac nha hang
    @Column(name = "res_domain", nullable = false, unique = true)
    private String resDomain;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;
}
