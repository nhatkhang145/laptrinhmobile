package com.zappy.controller;

import com.zappy.entity.MenuItem;
import com.zappy.entity.Order;
import com.zappy.entity.OrderDetail;
import com.zappy.entity.RestaurantTable;
import com.zappy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired private OrderRepository orderRepo;
    @Autowired private OrderDetailRepository detailRepo;
    @Autowired private TableRepository tableRepo;
    @Autowired private MenuItemRepository menuItemRepo;

    // Api mở bàn
    @PostMapping("/open")
    public ResponseEntity<?> openTable(@RequestBody Map<String, Integer> data) {
        Integer tableId = data.get("tableId");
        Integer userId = data.get("userId");

        // Lấy thông tin bàn từ db
        RestaurantTable table = tableRepo.findById(tableId).orElse(null);
        // Báo lỗi nếu bàn ko tồn tại
        if (table == null) return ResponseEntity.badRequest()
                .body(Map.of("message", "Không tìm thấy bàn!"));

        // Nếu bàn có người thì báo lỗi
        if (table.getIsOccupied()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Bàn này đang có khách!"));
        }

        com.zappy.entity.User user = null;
        if (userId != null) {
            user = new com.zappy.entity.User();
            user.setId(userId);
        }

        // Tạo order mới trạng thái 0 (đang phục vụ)
        Order order = new Order();
        order.setTable(table);
        order.setUser(user);
        order.setStatus(0);
        order.setTotalAmount(BigDecimal.ZERO);
        Order savedOrder = orderRepo.save(order);

        // Cập nhật trạng thái bàn đang có khách
        table.setIsOccupied(true);
        tableRepo.save(table);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }

    // Lấy thông tin order đang phục vụ của 1 bàn
    @GetMapping("/table/{tableId}/active")
    public ResponseEntity<?> getActiveOrder(@PathVariable Integer tableId) {
        return orderRepo.findByTableIdAndStatus(tableId, 0)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Lấy danh sách order đang phục vụ của nhà hàng
    @GetMapping("/restaurant/{resId}/active")
    public ResponseEntity<?> getActiveOrdersByRestaurant(@PathVariable Integer resId) {
        List<Order> activeOrders = orderRepo.findByRestaurantIdAndStatus(resId, 0);

        // Cập nhật lại tổng tiền tạm tính cho các order
        for (Order order : activeOrders) {
            BigDecimal total = detailRepo.calculateTotalAmount(order.getId());
            if (total != null) {
                order.setTotalAmount(total);
            }
        }
        return ResponseEntity.ok(activeOrders);
    }

    // Lấy danh sách order đã thanh toán
    @GetMapping("/restaurant/{resId}/paid")
    public ResponseEntity<?> getPaidOrdersByRestaurant(
            @PathVariable Integer resId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {

        java.time.LocalDateTime from = null;
        java.time.LocalDateTime to = null;
        try {
            if (fromDate != null && !fromDate.isEmpty()) from = java.time.LocalDateTime.parse(fromDate);
            if (toDate != null && !toDate.isEmpty()) to = java.time.LocalDateTime.parse(toDate);
        } catch (Exception e) {
        }

        // Lấy ds hóa đơn status 1 theo khoảng tg
        List<Order> paidOrders = orderRepo.findPaidOrdersWithFilter(resId, 1, from, to);
        
        // Sắp xếp ưu tiên ngày thanh toán mới nhất
        paidOrders.sort((o1, o2) -> {
            if (o1.getCheckoutAt() != null && o2.getCheckoutAt() != null) {
                return o2.getCheckoutAt().compareTo(o1.getCheckoutAt());
            }
            if (o1.getCreatedAt() != null && o2.getCreatedAt() != null) {
                return o2.getCreatedAt().compareTo(o1.getCreatedAt());
            }
            return Integer.compare(o2.getId(), o1.getId());
        });
        
        return ResponseEntity.ok(paidOrders);
    }

    // Lấy chi tiết order bằng id
    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Integer id) {
        return orderRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Api thêm món ăn vào order
    @PostMapping("/{orderId}/items")
    public ResponseEntity<?> addItem(@PathVariable Integer orderId,
                                     @RequestBody Map<String, Object> data) {
        Integer itemId   = (Integer) data.get("itemId");
        Integer quantity = (Integer) data.get("quantity");
        String  note     = (String)  data.get("note");

        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null) return ResponseEntity.badRequest()
                .body(Map.of("message", "Không tìm thấy hóa đơn!"));
        // Chỉ thêm món khi order chưa thanh toán
        if (order.getStatus() != 0) return ResponseEntity.badRequest()
                .body(Map.of("message", "Hóa đơn đã đóng!"));

        MenuItem menuItem = menuItemRepo.findById(itemId).orElse(null);
        if (menuItem == null) return ResponseEntity.badRequest()
                .body(Map.of("message", "Không tìm thấy món ăn!"));

        // Tạo chi tiết order trạng thái 0 (nhập)
        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);
        detail.setMenuItem(menuItem);
        detail.setQuantity(quantity);
        detail.setPriceAtSale(menuItem.getPrice());
        detail.setNote(note);
        detail.setStatus(0);

        return ResponseEntity.status(HttpStatus.CREATED).body(detailRepo.save(detail));
    }

    // Api thêm nhiều món cùng lúc
    @PostMapping("/{orderId}/items/batch")
    public ResponseEntity<?> addBatchItems(@PathVariable Integer orderId,
                                           @RequestBody List<Map<String, Object>> itemsData) {
        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null) return ResponseEntity.badRequest()
                .body(Map.of("message", "Không tìm thấy hóa đơn!"));
        if (order.getStatus() != 0) return ResponseEntity.badRequest()
                .body(Map.of("message", "Hóa đơn đã đóng!"));

        List<OrderDetail> detailsToSave = new java.util.ArrayList<>();

        // Duyệt danh sách data thêm vào mảng details
        for (Map<String, Object> data : itemsData) {
            Integer itemId   = (Integer) data.get("itemId");
            Integer quantity = (Integer) data.get("quantity");
            String  note     = (String)  data.get("note");

            MenuItem menuItem = menuItemRepo.findById(itemId).orElse(null);
            if (menuItem != null && quantity != null && quantity > 0) {
                OrderDetail detail = new OrderDetail();
                detail.setOrder(order);
                detail.setMenuItem(menuItem);
                detail.setQuantity(quantity);
                detail.setPriceAtSale(menuItem.getPrice());
                detail.setNote(note);
                detail.setStatus(0);
                detailsToSave.add(detail);
            }
        }

        // Lưu ds xuống db
        detailRepo.saveAll(detailsToSave);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Thêm thành công " + detailsToSave.size() + " món"));
    }

    // Lấy ds món của hóa đơn
    @GetMapping("/{orderId}/details")
    public List<OrderDetail> getDetails(@PathVariable Integer orderId) {
        return detailRepo.findByOrderId(orderId);
    }

    // Chuyển món từ trạng thái nhập lên bếp
    @PutMapping("/{orderId}/send")
    public ResponseEntity<?> sendOrder(@PathVariable Integer orderId) {
        // Lấy ds món trạng thái 0
        List<OrderDetail> draftItems = detailRepo.findByOrderIdAndStatus(orderId, 0);

        if (draftItems.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Không có món nào trong giỏ!"));
        }

        // Set thành 1 (đã gửi lên bếp)
        draftItems.forEach(d -> d.setStatus(1));
        detailRepo.saveAll(draftItems);

        return ResponseEntity.ok(Map.of(
                "message", "Đã gửi " + draftItems.size() + " món lên bếp!",
                "sentCount", draftItems.size()
        ));
    }

    // Api hủy món ăn (cho quản lý)
    @PutMapping("/details/{detailId}/cancel")
    public ResponseEntity<?> cancelItem(@PathVariable Integer detailId,
                                        @RequestParam(value = "cancelReason", required = false) String cancelReason,
                                        @RequestBody(required = false) Map<String, Integer> data) {
        return detailRepo.findById(detailId).map(detail -> {
            if (detail.getStatus() == 2) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Món này đã bị hủy rồi!"));
            }

            // Chuyển thành trạng thái 2 (đã hủy)
            detail.setStatus(2);

            if (cancelReason != null && !cancelReason.trim().isEmpty()) {
                detail.setCancelReason(cancelReason.trim());
            }

            detail.setUpdatedAt(java.time.LocalDateTime.now());
            detailRepo.save(detail);

            return ResponseEntity.ok(Map.of("message", "Đã hủy món! Giữ lại để đối soát."));
        }).orElse(ResponseEntity.notFound().build());
    }
    
    // Thống kê ds món bị hủy
    @GetMapping("/restaurant/{resId}/cancelled")
    public ResponseEntity<?> getCancelledOrdersByRestaurant(
            @PathVariable Integer resId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {

        java.time.LocalDateTime from = null;
        java.time.LocalDateTime to = null;
        try {
            if (fromDate != null && !fromDate.isEmpty()) from = java.time.LocalDateTime.parse(fromDate);
            if (toDate != null && !toDate.isEmpty()) to = java.time.LocalDateTime.parse(toDate);
        } catch (Exception e) {
        }

        // Nếu ko truyền tham số ngày thì mặc định lấy trong hôm nay
        if (from == null) {
            from = java.time.LocalDate.now().atStartOfDay();
        }
        if (to == null) {
            to = java.time.LocalDate.now().atTime(23, 59, 59);
        }

        List<OrderDetail> cancelledItems = detailRepo.findCancelledItems(resId, from, to);
        return ResponseEntity.ok(cancelledItems);
    }

    // Api thanh toán hóa đơn
    @PostMapping("/{orderId}/checkout")
    public ResponseEntity<?> checkout(@PathVariable Integer orderId) {
        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null) return ResponseEntity.notFound().build();
        if (order.getStatus() != 0) return ResponseEntity.badRequest()
                .body(Map.of("message", "Hóa đơn đã đóng!"));

        // Tính tổng tiền các món ko bị hủy
        BigDecimal total = detailRepo.calculateTotalAmount(orderId);

        // Lưu thông tin thanh toán vào db
        order.setTotalAmount(total);
        order.setStatus(1);
        order.setCheckoutAt(java.time.LocalDateTime.now());
        orderRepo.save(order);

        // Set bàn trống trở lại
        RestaurantTable table = order.getTable();
        table.setIsOccupied(false);
        tableRepo.save(table);

        return ResponseEntity.ok(Map.of(
                "message", "Thanh toán thành công!",
                "totalAmount", total,
                "orderId", orderId
        ));
    }

    // Api thống kê doanh thu theo thời gian
    @GetMapping("/stats/restaurant/{resId}")
    public ResponseEntity<?> getDashboardStats(
            @PathVariable Integer resId,
            @RequestParam(defaultValue = "today") String period) {

        LocalDateTime startDate;
        LocalDateTime endDate;
        LocalDate today = LocalDate.now();

        // Gán thời gian theo params truyền vào
        switch (period.toLowerCase()) {
            case "week":
                startDate = today.with(DayOfWeek.MONDAY).atStartOfDay();
                endDate = startDate.plusWeeks(1);
                break;
            case "month":
                startDate = today.withDayOfMonth(1).atStartOfDay();
                endDate = startDate.plusMonths(1);
                break;
            case "today":
            default:
                startDate = today.atStartOfDay();
                endDate = startDate.plusDays(1);
                break;
        }

        // Tính tổng doanh thu
        BigDecimal revenue = orderRepo.getRevenue(resId, startDate, endDate);
        // Đếm số lượng order
        Long totalOrders = orderRepo.countOrders(resId, startDate, endDate);
        // Đếm số món bị hủy
        Long cancelledItems = detailRepo.countCancelledItems(resId, startDate, endDate);
        // Đếm bàn đang có khách
        Long unpaidTables = tableRepo.countOccupiedTables(resId);

        Map<String, Object> result = new HashMap<>();
        result.put("totalRevenue", revenue);
        result.put("totalOrders", totalOrders);
        result.put("cancelledItems", cancelledItems);
        result.put("unpaidTables", unpaidTables);

        return ResponseEntity.ok(result);
    }
}
