package com.zappy.repository;

import com.zappy.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableRepository extends JpaRepository<RestaurantTable, Integer> {
    // Lay tat ca ban cua 1 khu vuc
    List<RestaurantTable> findByAreaId(Integer areaId);
    // Lay ban trong (cho phep nhan vien chon)
    List<RestaurantTable> findByAreaIdAndIsOccupied(Integer areaId, Boolean isOccupied);
    // Lay tat ca ban cua nha hang
    List<RestaurantTable> findByAreaRestaurantId(Integer resId);
    
    @Query("""
    		SELECT COUNT(t)
    		FROM RestaurantTable t
    		WHERE t.area.restaurant.id=:resId
    		AND t.isOccupied=true
    		""")
    		Long countOccupiedTables(
    		        @Param("resId") Integer resId
    		);
}
