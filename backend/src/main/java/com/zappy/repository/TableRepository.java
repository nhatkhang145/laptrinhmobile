package com.zappy.repository;

import com.zappy.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ============================================================
 *  TABLE REPOSITORY - THAO TÁC CSDL BẢNG tables
 * ============================================================
 *
 * Quản lý danh sách bàn ăn của nhà hàng.
 * Mỗi bàn thuộc 1 khu vực (Area), mỗi khu vực thuộc 1 nhà hàng (Restaurant).
 *
 * Quan hệ: RestaurantTable → Area → Restaurant
 */
@Repository
public interface TableRepository extends JpaRepository<RestaurantTable, Integer> {

    /**
     * LẤY TẤT CẢ BÀN TRONG 1 KHU VỰC
     *
     * Spring JPA tự sinh SQL:
     *   SELECT * FROM tables WHERE area_id = ?
     *
     * Dùng khi: Hiển thị sơ đồ bàn theo khu vực (trong/ngoài/sân thượng...)
     *
     * @param areaId ID khu vực
     * @return Danh sách tất cả bàn trong khu vực
     */
    List<RestaurantTable> findByAreaId(Integer areaId);

    /**
     * LẤY BÀN THEO TRẠNG THÁI TRONG 1 KHU VỰC
     *
     * Spring JPA tự sinh SQL:
     *   SELECT * FROM tables WHERE area_id = ? AND is_occupied = ?
     *
     * Dùng khi:
     *   - Lấy bàn trống (isOccupied = false) để nhân viên chọn cho khách mới
     *   - Lấy bàn đang có khách (isOccupied = true) để hiển thị màu đỏ
     *
     * @param areaId     ID khu vực
     * @param isOccupied true = bàn có khách, false = bàn trống
     * @return Danh sách bàn theo trạng thái
     */
    List<RestaurantTable> findByAreaIdAndIsOccupied(Integer areaId, Boolean isOccupied);

    /**
     * KIỂM TRA KHU VỰC CÓ BÀN NÀO ĐANG CÓ KHÁCH KHÔNG
     *
     * Spring JPA tự sinh SQL:
     *   SELECT COUNT(*) > 0 FROM tables WHERE area_id = ? AND is_occupied = true
     *
     * Trả về boolean: true nếu có ít nhất 1 bàn đang bận
     *
     * Dùng khi: Kiểm tra trước khi cho phép thực hiện thao tác trên khu vực
     * (ví dụ: không xóa khu vực đang có bàn bận)
     *
     * @param areaId ID khu vực
     * @return true nếu khu vực có bàn đang phục vụ
     */
    boolean existsByAreaIdAndIsOccupiedTrue(Integer areaId);

    /**
     * LẤY TẤT CẢ BÀN CỦA TOÀN NHÀ HÀNG
     *
     * Spring JPA tự sinh SQL (duyệt qua quan hệ Area → Restaurant):
     *   SELECT t.* FROM tables t
     *     JOIN areas a ON t.area_id = a.id
     *   WHERE a.restaurant_id = ?
     *
     * Dùng khi: Quản lý muốn xem tổng quan tất cả bàn của nhà hàng
     *
     * @param resId ID nhà hàng
     * @return Danh sách tất cả bàn trong nhà hàng
     */
    List<RestaurantTable> findByAreaRestaurantId(Integer resId);

    /**
     * ĐẾM SỐ BÀN ĐANG CÓ KHÁCH (CHƯA THANH TOÁN) TRONG TOÀN NHÀ HÀNG
     *
     * JPQL:
     *   SELECT COUNT(t)
     *   FROM RestaurantTable t
     *   WHERE t.area.restaurant.id = :resId   ← Duyệt qua: Table → Area → Restaurant
     *   AND t.isOccupied = true               ← Chỉ đếm bàn đang có khách
     *
     * Tương đương SQL:
     *   SELECT COUNT(*) FROM tables t
     *     JOIN areas a ON t.area_id = a.id
     *   WHERE a.restaurant_id = ? AND t.is_occupied = true
     *
     * Dùng cho Dashboard - ô "Số bàn đang phục vụ"
     *
     * @param resId ID nhà hàng
     * @return Số bàn đang có khách (Long)
     */
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
