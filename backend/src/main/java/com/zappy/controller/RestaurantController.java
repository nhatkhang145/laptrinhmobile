package com.zappy.controller;

import com.zappy.entity.Restaurant;
import com.zappy.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API Quan ly Nha hang
 * GET  /api/restaurants          -> Lay danh sach nha hang
 * GET  /api/restaurants/{id}     -> Lay 1 nha hang
 * GET  /api/restaurants/domain/{domain} -> Tim theo domain
 * POST /api/restaurants          -> Tao nha hang moi
 * PUT  /api/restaurants/{id}     -> Cap nhat nha hang
 */
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    @Autowired
    private RestaurantRepository restaurantRepo;

    @GetMapping
    public List<Restaurant> getAll() {
        return restaurantRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getById(@PathVariable Integer id) {
        return restaurantRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/domain/{domain}")
    public ResponseEntity<?> getByDomain(@PathVariable String domain) {
        return restaurantRepo.findByResDomain(domain)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Restaurant restaurant) {
        if (restaurantRepo.existsByResDomain(restaurant.getResDomain())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Domain '" + restaurant.getResDomain() + "' da ton tai!"));
        }
        Restaurant saved = restaurantRepo.save(restaurant);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Restaurant> update(@PathVariable Integer id,
                                             @RequestBody Restaurant data) {
        return restaurantRepo.findById(id).map(r -> {
            r.setResName(data.getResName());
            r.setAddress(data.getAddress());
            return ResponseEntity.ok(restaurantRepo.save(r));
        }).orElse(ResponseEntity.notFound().build());
    }
}
