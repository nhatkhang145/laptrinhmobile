package com.zappy.repository;

import com.zappy.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AreaRepository extends JpaRepository<Area, Integer> {
    // Lay tat ca khu vuc cua 1 nha hang
    List<Area> findByRestaurantId(Integer resId);
}
