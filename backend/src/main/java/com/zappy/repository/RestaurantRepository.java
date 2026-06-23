package com.zappy.repository;

import com.zappy.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {
    // Tim nha hang theo domain (dung de dang nhap)
    Optional<Restaurant> findByResDomain(String resDomain);
    boolean existsByResDomain(String resDomain);
}
