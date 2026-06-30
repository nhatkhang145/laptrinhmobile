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
    /**
     * Lấy danh sách tất cả các ca làm việc của một nhà hàng.
     * Dữ liệu được sắp xếp theo thời gian mở ca giảm dần
     * để hiển thị lịch sử các ca gần nhất trước.
     */
    @GetMapping("/restaurant/{resId}")
    public ResponseEntity<List<Shift>> getAllShifts(@PathVariable Long resId) {
        // Truy vấn danh sách ca theo nhà hàng
        List<Shift> shifts = shiftRepository.findByRestaurantIdOrderByStartTimeDesc(resId);
        // Trả về danh sách ca
        return ResponseEntity.ok(shifts);
    }

    // Kiểm tra xem có ca nào đang mở không
    /**
     * Kiểm tra nhà hàng hiện có ca làm việc đang mở hay không.
     * Nếu có thì tính doanh thu từ lúc mở ca đến thời điểm hiện tại.
     */
    @GetMapping("/restaurant/{resId}/active")
    public ResponseEntity<?> getActiveShift(@PathVariable Long resId) {
        // Tìm ca đang mở
        Optional<Shift> activeShift = shiftRepository.findByRestaurantIdAndStatus(resId, "OPEN");
        if (activeShift.isPresent()) {
            Shift shift = activeShift.get();
            // Tính doanh thu của ca từ lúc mở ca đến hiện tại
            java.math.BigDecimal revenue = orderRepository.getRevenue(resId.intValue(), shift.getStartTime(), LocalDateTime.now());
            // Nếu có doanh thu thì cập nhật vào ca
            if (revenue != null) {
                shift.setTotalRevenue(revenue.doubleValue());
            } else {
                shift.setTotalRevenue(0.0);
            }
            return ResponseEntity.ok(shift);
        } else {
            // Không có ca đang mở
            return ResponseEntity.notFound().build();
        }
    }

    // Mở ca mới
    /**
     * Mở một ca làm việc mới.
     *
     * Điều kiện:
     * - Chỉ được phép tồn tại một ca OPEN tại một thời điểm.
     */
    @PostMapping("/open")
    public ResponseEntity<?> openShift(@RequestBody Shift shiftRequest) {
        // Kiểm tra xem đã có ca nào đang mở chưa
        Optional<Shift> activeShift = shiftRepository.findByRestaurantIdAndStatus(shiftRequest.getRestaurantId(), "OPEN");
        if (activeShift.isPresent()) {
            // Không cho mở thêm nếu đã có ca OPEN
            return ResponseEntity.badRequest().body("Đã có một ca làm việc đang mở. Vui lòng đóng ca hiện tại trước khi mở ca mới.");
        }
        // Tạo ca mới
        Shift newShift = new Shift();
        newShift.setRestaurantId(shiftRequest.getRestaurantId());
        newShift.setStartingFund(shiftRequest.getStartingFund());
        newShift.setEmployeeNames(shiftRequest.getEmployeeNames());
        newShift.setEmployeeIds(shiftRequest.getEmployeeIds());
        // Thiết lập thông tin mặc định khi mở ca
        newShift.setStartTime(LocalDateTime.now());
        newShift.setStatus("OPEN");
        newShift.setTotalRevenue(0.0);
        // Lưu ca vào cơ sở dữ liệu
        Shift savedShift = shiftRepository.save(newShift);
        return ResponseEntity.ok(savedShift);
    }

    // Cập nhật nhân viên cho ca đang hoạt động
    /**
     * Cập nhật danh sách nhân viên của ca làm việc.
     *
     * Chỉ cho phép cập nhật khi ca vẫn đang OPEN.
     */
    @PutMapping("/{id}/employees")
    public ResponseEntity<?> updateShiftEmployees(@PathVariable Long id, @RequestBody Shift updateRequest) {
        // Tìm ca theo id
        Optional<Shift> optionalShift = shiftRepository.findById(id);
        if (optionalShift.isPresent()) {
            Shift shift = optionalShift.get();
            // Không cho sửa nếu ca đã đóng
            if ("CLOSED".equals(shift.getStatus())) {
                return ResponseEntity.badRequest().body("Ca làm việc này đã đóng, không thể sửa nhân viên.");
            }
            // Cập nhật danh sách nhân viên
            shift.setEmployeeNames(updateRequest.getEmployeeNames());
            shift.setEmployeeIds(updateRequest.getEmployeeIds());
            // Lưu thay đổi
            Shift savedShift = shiftRepository.save(shift);
            return ResponseEntity.ok(savedShift);
        } else {
            // Không tìm thấy ca
            return ResponseEntity.notFound().build();
        }
    }

    // Đóng ca
    /**
     * Đóng ca làm việc.
     *
     * Khi đóng ca sẽ:
     * - Cập nhật trạng thái CLOSED.
     * - Lưu thời gian kết thúc.
     * - Cập nhật tổng doanh thu của ca.
     */
    @PutMapping("/{id}/close")
    public ResponseEntity<?> closeShift(@PathVariable Long id, @RequestBody Shift closeRequest) {
        // Tìm ca theo id
        Optional<Shift> optionalShift = shiftRepository.findById(id);
        if (optionalShift.isPresent()) {
            Shift shift = optionalShift.get();
            // Không cho đóng lại nếu ca đã đóng
            if ("CLOSED".equals(shift.getStatus())) {
                return ResponseEntity.badRequest().body("Ca làm việc này đã được đóng trước đó.");
            }
            // Cập nhật thông tin đóng ca
            shift.setStatus("CLOSED");
            shift.setEndTime(LocalDateTime.now());
            // Frontend truyền lên hoặc Backend tính tổng từ bảng Order
            if (closeRequest.getTotalRevenue() != null) {
                shift.setTotalRevenue(closeRequest.getTotalRevenue());
            }
            // Lưu thông tin ca sau khi đóng
            Shift savedShift = shiftRepository.save(shift);
            return ResponseEntity.ok(savedShift);
        } else {
            // Không tìm thấy ca
            return ResponseEntity.notFound().build();
        }
    }
}
