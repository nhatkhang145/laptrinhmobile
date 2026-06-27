package com.zappy.repository;

import com.zappy.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findByRestaurantIdOrderByStartTimeDesc(Long restaurantId);
    Optional<Shift> findByRestaurantIdAndStatus(Long restaurantId, String status);
}
