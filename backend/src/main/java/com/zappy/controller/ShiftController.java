package com.zappy.controller;

import com.zappy.entity.Shift;
import com.zappy.repository.ShiftRepository;
import com.zappy.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private OrderRepository orderRepository;

    // Lấy danh sách tất cả các ca (Lịch sử)
    @GetMapping("/restaurant/{resId}")
    public ResponseEntity<List<Shift>> getAllShifts(@PathVariable Long resId) {
        List<Shift> shifts = shiftRepository.findByRestaurantIdOrderByStartTimeDesc(resId);
        return ResponseEntity.ok(shifts);
    }

    // Kiểm tra xem có ca nào đang mở không
    @GetMapping("/restaurant/{resId}/active")
    public ResponseEntity<?> getActiveShift(@PathVariable Long resId) {
        Optional<Shift> activeShift = shiftRepository.findByRestaurantIdAndStatus(resId, "OPEN");
        if (activeShift.isPresent()) {
            Shift shift = activeShift.get();
            java.math.BigDecimal revenue = orderRepository.getRevenue(resId.intValue(), shift.getStartTime(), LocalDateTime.now());
            if (revenue != null) {
                shift.setTotalRevenue(revenue.doubleValue());
            } else {
                shift.setTotalRevenue(0.0);
            }
            return ResponseEntity.ok(shift);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Mở ca mới
    @PostMapping("/open")
    public ResponseEntity<?> openShift(@RequestBody Shift shiftRequest) {
        // Kiểm tra xem đã có ca nào đang mở chưa
        Optional<Shift> activeShift = shiftRepository.findByRestaurantIdAndStatus(shiftRequest.getRestaurantId(), "OPEN");
        if (activeShift.isPresent()) {
            return ResponseEntity.badRequest().body("Đã có một ca làm việc đang mở. Vui lòng đóng ca hiện tại trước khi mở ca mới.");
        }

        Shift newShift = new Shift();
        newShift.setRestaurantId(shiftRequest.getRestaurantId());
        newShift.setStartingFund(shiftRequest.getStartingFund());
        newShift.setEmployeeNames(shiftRequest.getEmployeeNames());
        newShift.setEmployeeIds(shiftRequest.getEmployeeIds());
        newShift.setStartTime(LocalDateTime.now());
        newShift.setStatus("OPEN");
        newShift.setTotalRevenue(0.0);

        Shift savedShift = shiftRepository.save(newShift);
        return ResponseEntity.ok(savedShift);
    }

    // Cập nhật nhân viên cho ca đang hoạt động
    @PutMapping("/{id}/employees")
    public ResponseEntity<?> updateShiftEmployees(@PathVariable Long id, @RequestBody Shift updateRequest) {
        Optional<Shift> optionalShift = shiftRepository.findById(id);
        if (optionalShift.isPresent()) {
            Shift shift = optionalShift.get();
            if ("CLOSED".equals(shift.getStatus())) {
                return ResponseEntity.badRequest().body("Ca làm việc này đã đóng, không thể sửa nhân viên.");
            }
            
            shift.setEmployeeNames(updateRequest.getEmployeeNames());
            shift.setEmployeeIds(updateRequest.getEmployeeIds());
            
            Shift savedShift = shiftRepository.save(shift);
            return ResponseEntity.ok(savedShift);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Đóng ca
    @PutMapping("/{id}/close")
    public ResponseEntity<?> closeShift(@PathVariable Long id, @RequestBody Shift closeRequest) {
        Optional<Shift> optionalShift = shiftRepository.findById(id);
        if (optionalShift.isPresent()) {
            Shift shift = optionalShift.get();
            if ("CLOSED".equals(shift.getStatus())) {
                return ResponseEntity.badRequest().body("Ca làm việc này đã được đóng trước đó.");
            }
            
            shift.setStatus("CLOSED");
            shift.setEndTime(LocalDateTime.now());
            // Frontend truyền lên hoặc Backend tính tổng từ bảng Order
            if (closeRequest.getTotalRevenue() != null) {
                shift.setTotalRevenue(closeRequest.getTotalRevenue());
            }

            Shift savedShift = shiftRepository.save(shift);
            return ResponseEntity.ok(savedShift);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
