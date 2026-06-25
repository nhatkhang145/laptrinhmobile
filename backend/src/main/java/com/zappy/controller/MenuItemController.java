package com.zappy.controller;

import com.zappy.entity.Category;
import com.zappy.entity.MenuItem;
import com.zappy.entity.Unit;
import com.zappy.repository.CategoryRepository;
import com.zappy.repository.MenuItemRepository;
import com.zappy.repository.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/menu-items")
public class MenuItemController {

    @Autowired private MenuItemRepository menuItemRepo;
    @Autowired private CategoryRepository categoryRepo;
    @Autowired private UnitRepository unitRepo;

    // Lay tat ca mon an cua 1 nha hang (co ho tro tim kiem)
    @GetMapping("/restaurant/{resId}")
    public List<MenuItem> getByRestaurant(@PathVariable Integer resId,
                                          @RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return menuItemRepo.findByCategoryRestaurantIdAndItemNameContainingIgnoreCase(resId, keyword.trim());
        }
        return menuItemRepo.findByCategoryRestaurantId(resId);
    }

    // API Thong ke: Tong mon, Dang ban, Het hang
    @GetMapping("/restaurant/{resId}/stats")
    public ResponseEntity<Map<String, Long>> getStats(@PathVariable Integer resId) {
        long total = menuItemRepo.countByCategoryRestaurantId(resId);
        long available = menuItemRepo.countByCategoryRestaurantIdAndIsAvailable(resId, true);
        long outOfStock = total - available;
        return ResponseEntity.ok(Map.of(
                "total", total,
                "available", available,
                "outOfStock", outOfStock
        ));
    }

    // Lay mon an theo danh muc (co ho tro tim kiem)
    @GetMapping("/category/{catId}")
    public List<MenuItem> getByCategory(@PathVariable Integer catId,
                                        @RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return menuItemRepo.findByCategoryIdAndItemNameContainingIgnoreCase(catId, keyword.trim());
        }
        return menuItemRepo.findByCategoryId(catId);
    }

    // Lay 1 mon an
    @GetMapping("/{id}")
    public ResponseEntity<MenuItem> getById(@PathVariable Integer id) {
        return menuItemRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Them mon moi vao menu
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> data) {
        Integer catId    = (Integer) data.get("catId");
        Integer unitId   = (Integer) data.get("unitId");
        String itemName  = (String)  data.get("itemName");
        Number priceRaw  = (Number)  data.get("price");  // co the la Integer hoac Double

        Category category = categoryRepo.findById(catId).orElse(null);
        Unit unit         = unitRepo.findById(unitId).orElse(null);

        if (category == null || unit == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Danh muc hoac don vi tinh khong hop le!"));
        }

        MenuItem item = new MenuItem();
        item.setCategory(category);
        item.setUnit(unit);
        item.setItemName(itemName);
        item.setPrice(BigDecimal.valueOf(priceRaw.doubleValue()));
        if (data.containsKey("isAvailable")) {
            item.setIsAvailable((Boolean) data.get("isAvailable"));
        }
        if (data.containsKey("imageUrl")) {
            item.setImageUrl((String) data.get("imageUrl"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(menuItemRepo.save(item));
    }

    // Cap nhat mon an
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id,
                                    @RequestBody Map<String, Object> data) {
        return menuItemRepo.findById(id).map(item -> {
            if (data.containsKey("itemName"))
                item.setItemName((String) data.get("itemName"));
            if (data.containsKey("price")) {
                Number p = (Number) data.get("price");
                item.setPrice(BigDecimal.valueOf(p.doubleValue()));
            }
            if (data.containsKey("isAvailable")) {
                item.setIsAvailable((Boolean) data.get("isAvailable"));
            }
            if (data.containsKey("imageUrl")) {
                item.setImageUrl((String) data.get("imageUrl"));
            }
            if (data.containsKey("catId")) {
                Integer catId = (Integer) data.get("catId");
                Category category = categoryRepo.findById(catId).orElse(null);
                if (category != null) item.setCategory(category);
            }
            if (data.containsKey("unitId")) {
                Integer unitId = (Integer) data.get("unitId");
                Unit unit = unitRepo.findById(unitId).orElse(null);
                if (unit != null) item.setUnit(unit);
            }
            return ResponseEntity.ok(menuItemRepo.save(item));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        if (!menuItemRepo.existsById(id)) return ResponseEntity.notFound().build();
        menuItemRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Da xoa mon an!"));
    }
}
