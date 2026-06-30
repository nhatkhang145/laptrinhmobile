package com.zappy.repository;

import com.zappy.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableRepository extends JpaRepository<RestaurantTable, Integer> {

    // Lấy danh sách bàn theo khu vực
    List<RestaurantTable> findByAreaId(Integer areaId);

    // Lấy bàn theo trạng thái trong khu vực
    List<RestaurantTable> findByAreaIdAndIsOccupied(Integer areaId, Boolean isOccupied);

    // Kiểm tra xem khu vực có bàn nào đang bận không
    boolean existsByAreaIdAndIsOccupiedTrue(Integer areaId);

    // Lấy toàn bộ bàn của nhà hàng
    List<RestaurantTable> findByAreaRestaurantId(Integer resId);

    // Đếm số bàn đang có khách để hiển thị thống kê
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
