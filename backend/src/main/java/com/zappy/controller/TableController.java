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
 * GET  /api/tables/area/{areaId}    -> Lay tat ca ban trong 1 khu vuc
 * PUT  /api/tables/{id}/status      -> Cap nhat trang thai ban (trong/co khach)
 * POST /api/tables                  -> Tao ban moi
 * DELETE /api/tables/{id}           -> Xoa ban
 */
@RestController
@RequestMapping("/api/tables")
public class TableController {

    @Autowired private TableRepository tableRepo;
    @Autowired private AreaRepository areaRepo;
    @Autowired private com.zappy.repository.OrderRepository orderRepo;

    // Lay tat ca ban trong khu vuc
    @GetMapping("/area/{areaId}")
    public List<RestaurantTable> getByArea(@PathVariable Integer areaId) {
        return tableRepo.findByAreaId(areaId);
    }

    // Lay tat ca ban trong nha hang (dung cho Danh sach Order)
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

    // Lay ban theo id
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantTable> getById(@PathVariable Integer id) {
        return tableRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Tao ban moi
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> data) {
        Integer areaId    = (Integer) data.get("areaId");
        String  tableName = (String)  data.get("tableName");

        Area area = areaRepo.findById(areaId).orElse(null);
        if (area == null) return ResponseEntity.badRequest()
                .body(Map.of("message", "Khong tim thay khu vuc!"));

        RestaurantTable table = new RestaurantTable();
        table.setArea(area);
        table.setTableName(tableName);
        table.setIsOccupied(false);
        return ResponseEntity.status(HttpStatus.CREATED).body(tableRepo.save(table));
    }

    // Cap nhat trang thai ban: trong / co khach
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id,
                                          @RequestBody Map<String, Boolean> data) {
        return tableRepo.findById(id).map(table -> {
            table.setIsOccupied(data.get("isOccupied"));
            return ResponseEntity.ok(tableRepo.save(table));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Doi ten ban
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id,
                                    @RequestBody Map<String, String> data) {
        return tableRepo.findById(id).map(table -> {
            table.setTableName(data.get("tableName"));
            return ResponseEntity.ok(tableRepo.save(table));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        if (!tableRepo.existsById(id)) return ResponseEntity.notFound().build();
        tableRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Da xoa ban!"));
    }
}
