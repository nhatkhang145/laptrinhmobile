package com.zappy.controller;

import com.zappy.entity.Area;
import com.zappy.entity.RestaurantTable;
import com.zappy.repository.AreaRepository;
import com.zappy.repository.TableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API Quan ly Ban an
 */
@RestController
@RequestMapping("/api/tables")
public class TableController {

    @Autowired private TableRepository tableRepo;
    @Autowired private AreaRepository areaRepo;
    @Autowired private com.zappy.repository.OrderRepository orderRepo;

    @GetMapping("/area/{areaId}")
    public List<RestaurantTable> getByArea(@PathVariable Integer areaId) {
        return tableRepo.findByAreaId(areaId);
    }

    @GetMapping("/restaurant/{resId}")
    public List<RestaurantTable> getByRestaurant(@PathVariable Integer resId) {
        List<RestaurantTable> tables = tableRepo.findByAreaRestaurantId(resId);
        for (RestaurantTable t : tables) {
            if (t.getIsOccupied()) {
                orderRepo.findByTableIdAndStatus(t.getId(), 0).ifPresent(order -> {
                    if (order.getUser() != null) {
                        t.setActiveUserId(order.getUser().getId());
                    }
                });
            }
        }
        return tables;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantTable> getById(@PathVariable Integer id) {
        return tableRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> data) {
        // 1. Check null keys
        if (data.get("areaId") == null || data.get("tableName") == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thiếu areaId hoặc tableName!"));
        }

        // 2. Safe conversion (tránh ClassCastException từ Long/Integer)
        Integer areaId;
        try {
            areaId = Integer.parseInt(data.get("areaId").toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "areaId không hợp lệ!"));
        }

        // 3. Validation chuỗi rỗng
        String tableName = data.get("tableName").toString().trim();
        if (tableName.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tên bàn không được để trống!"));
        }

        Area area = areaRepo.findById(areaId).orElse(null);
        if (area == null) return ResponseEntity.badRequest()
                .body(Map.of("message", "Khong tim thay khu vuc!"));

        RestaurantTable table = new RestaurantTable();
        table.setArea(area);
        table.setTableName(tableName);
        table.setIsOccupied(false);
        if (data.containsKey("seats") && data.get("seats") != null) {
            try { table.setSeats(Integer.parseInt(data.get("seats").toString())); } catch (Exception ignored) {}
        }
        if (data.containsKey("status") && data.get("status") != null) {
            table.setStatus(data.get("status").toString().trim());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(tableRepo.save(table));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id,
                                          @RequestBody Map<String, Boolean> data) {
        if (data.get("isOccupied") == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thiếu isOccupied!"));
        }
        return tableRepo.findById(id).map(table -> {
            table.setIsOccupied(data.get("isOccupied"));
            return ResponseEntity.ok(tableRepo.save(table));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id,
                                    @RequestBody Map<String, Object> data) {
        if (data.get("tableName") == null || data.get("tableName").toString().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tên bàn không hợp lệ!"));
        }
        return tableRepo.findById(id).map(table -> {
            table.setTableName(data.get("tableName").toString().trim());
            if (data.containsKey("seats") && data.get("seats") != null) {
                try { table.setSeats(Integer.parseInt(data.get("seats").toString())); } catch (Exception ignored) {}
            }
            if (data.containsKey("status") && data.get("status") != null) {
                table.setStatus(data.get("status").toString().trim());
            }
            if (data.containsKey("areaId") && data.get("areaId") != null) {
                try {
                    Integer newAreaId = Integer.parseInt(data.get("areaId").toString());
                    areaRepo.findById(newAreaId).ifPresent(table::setArea);
                } catch (Exception ignored) {}
            }
            return ResponseEntity.ok(tableRepo.save(table));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        if (!tableRepo.existsById(id)) return ResponseEntity.notFound().build();
        try {
            tableRepo.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Da xoa ban!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Không thể xóa bàn đã có hóa đơn/khách"));
        }
    }
}
