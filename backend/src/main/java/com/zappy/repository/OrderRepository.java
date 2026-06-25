package com.zappy.repository;

import com.zappy.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    // Tim hoa don dang phuc vu (status=0) cua 1 ban
    Optional<Order> findByTableIdAndStatus(Integer tableId, Integer status);
    // Lich su hoa don cua 1 ban
    List<Order> findByTableId(Integer tableId);
}
