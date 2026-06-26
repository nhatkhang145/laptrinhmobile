package com.zappy.controller;

import com.zappy.entity.Category;
import com.zappy.entity.Restaurant;
import com.zappy.entity.Unit;
import com.zappy.repository.CategoryRepository;
import com.zappy.repository.RestaurantRepository;
import com.zappy.repository.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API Danh muc & Don vi tinh
 */
@RestController
public class CategoryUnitController {

    @Autowired private CategoryRepository categoryRepo;
    @Autowired private UnitRepository unitRepo;
    @Autowired private RestaurantRepository restaurantRepo;

    // ===== CATEGORIES =====
    @GetMapping("/api/categories/restaurant/{resId}")
    public List<Category> getCategories(@PathVariable Integer resId) {
        return categoryRepo.findByRestaurantId(resId);
    }

    @PostMapping("/api/categories")
    public ResponseEntity<?> createCategory(@RequestBody Map<String, Object> data) {
        if (data.get("resId") == null || data.get("catName") == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thiếu resId hoặc catName!"));
        }

        Integer resId;
        try {
            resId = Integer.parseInt(data.get("resId").toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "resId không hợp lệ!"));
        }

        String catName = data.get("catName").toString().trim();
        if (catName.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tên danh mục không được để trống!"));
        }

        Restaurant r = restaurantRepo.findById(resId).orElse(null);
        if (r == null) return ResponseEntity.badRequest()
                .body(Map.of("message", "Khong tim thay nha hang!"));

        Category c = new Category();
        c.setRestaurant(r);
        c.setCatName(catName);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryRepo.save(c));
    }

    @DeleteMapping("/api/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        if (!categoryRepo.existsById(id)) return ResponseEntity.notFound().build();
        categoryRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Da xoa danh muc!"));
    }

    // ===== UNITS =====
    @GetMapping("/api/units/restaurant/{resId}")
    public List<Unit> getUnits(@PathVariable Integer resId) {
        return unitRepo.findByRestaurantId(resId);
    }

    @PostMapping("/api/units")
    public ResponseEntity<?> createUnit(@RequestBody Map<String, Object> data) {
        if (data.get("resId") == null || data.get("unitName") == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thiếu resId hoặc unitName!"));
        }

        Integer resId;
        try {
            resId = Integer.parseInt(data.get("resId").toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "resId không hợp lệ!"));
        }

        String unitName = data.get("unitName").toString().trim();
        if (unitName.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tên đơn vị không được để trống!"));
        }

        Restaurant r = restaurantRepo.findById(resId).orElse(null);
        if (r == null) return ResponseEntity.badRequest()
                .body(Map.of("message", "Khong tim thay nha hang!"));

        Unit u = new Unit();
        u.setRestaurant(r);
        u.setUnitName(unitName);
        return ResponseEntity.status(HttpStatus.CREATED).body(unitRepo.save(u));
    }

    @DeleteMapping("/api/units/{id}")
    public ResponseEntity<?> deleteUnit(@PathVariable Integer id) {
        if (!unitRepo.existsById(id)) return ResponseEntity.notFound().build();
        unitRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Da xoa don vi tinh!"));
    }
}
