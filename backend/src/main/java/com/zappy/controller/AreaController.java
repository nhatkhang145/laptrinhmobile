package com.zappy.controller;

import com.zappy.entity.Area;
import com.zappy.entity.Restaurant;
import com.zappy.repository.AreaRepository;
import com.zappy.repository.RestaurantRepository;
import com.zappy.repository.TableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API Khu vuc phuc vu
 * GET  /api/areas/restaurant/{resId} -> Lay khu vuc cua nha hang
 * POST /api/areas                    -> Tao khu vuc moi
 * PUT  /api/areas/{id}              -> Doi ten khu vuc
 * DELETE /api/areas/{id}            -> Xoa khu vuc
 */
@RestController
@RequestMapping("/api/areas")
public class AreaController {

    @Autowired private AreaRepository areaRepo;
    @Autowired private RestaurantRepository restaurantRepo;
    @Autowired private TableRepository tableRepo;

    @GetMapping("/restaurant/{resId}")
    public List<Area> getByRestaurant(@PathVariable Integer resId) {
        return areaRepo.findByRestaurantId(resId);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> data) {
        Integer resId    = (Integer) data.get("resId");
        String areaName  = (String)  data.get("areaName");

        Restaurant restaurant = restaurantRepo.findById(resId).orElse(null);
        if (restaurant == null) return ResponseEntity.badRequest()
                .body(Map.of("message", "Khong tim thay nha hang!"));

        Area area = new Area();
        area.setRestaurant(restaurant);
        area.setAreaName(areaName);
        return ResponseEntity.status(HttpStatus.CREATED).body(areaRepo.save(area));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id,
                                    @RequestBody Map<String, String> data) {
        return areaRepo.findById(id).map(a -> {
            a.setAreaName(data.get("areaName"));
            return ResponseEntity.ok(areaRepo.save(a));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        if (!areaRepo.existsById(id)) return ResponseEntity.notFound().build();
        areaRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Da xoa khu vuc!"));
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id) {
        return areaRepo.findById(id).map(a -> {
            boolean currentStatus = a.getIsActive() != null ? a.getIsActive() : true;
            
            // Neu dang chuan bi an khu vuc, kiem tra xem co ban nao dang co khach khong
            if (currentStatus) {
                if (tableRepo.existsByAreaIdAndIsOccupiedTrue(id)) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Không thể ẩn khu vực đang có bàn có khách!"));
                }
            }
            
            a.setIsActive(!currentStatus);
            areaRepo.save(a);
            return ResponseEntity.ok(Map.of("isActive", a.getIsActive()));
        }).orElse(ResponseEntity.notFound().build());
    }
}
