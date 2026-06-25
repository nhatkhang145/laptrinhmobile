package com.zappy.repository;

import com.zappy.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {
    // Lay tat ca mon an trong 1 danh muc
    List<MenuItem> findByCategoryId(Integer catId);
    // Lay tat ca mon an cua 1 nha hang (
    List<MenuItem> findByCategoryRestaurantId(Integer resId);

    // Tim mon an theo ten cho 1 nha hang 
    List<MenuItem> findByCategoryRestaurantIdAndItemNameContainingIgnoreCase(Integer resId, String keyword);

    // Tim mon an theo ten trong 1 danh muc 
    List<MenuItem> findByCategoryIdAndItemNameContainingIgnoreCase(Integer catId, String keyword);

    // Thong ke mon an cua 1 nha hang
    long countByCategoryRestaurantId(Integer resId);
    long countByCategoryRestaurantIdAndIsAvailable(Integer resId, Boolean isAvailable);
}
