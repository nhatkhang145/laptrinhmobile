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

/**
 * API Hoa don - Nghiep vu chinh cua app
 *
 * POST /api/orders/open              -> Nhan vien mo ban (tao hoa don moi)
 * GET  /api/orders/{id}             -> Lay thong tin hoa don
 * GET  /api/orders/table/{tableId}  -> Lay hoa don dang phuc vu cua ban
 * POST /api/orders/{id}/checkout    -> Thanh toan & dong hoa don
 *
 * GET  /api/orders/{id}/details     -> Lay chi tiet mon cua hoa don
 * POST /api/orders/{id}/items       -> Nhan vien them mon vao gio (status=0)
 * PUT  /api/orders/{id}/send        -> Nhan vien gui mon -> status=1 (KHOA)
 * PUT  /api/orders/details/{detailId}/cancel -> QUAN LY huy mon -> status=2
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired private OrderRepository orderRepo;
    @Autowired private OrderDetailRepository detailRepo;
    @Autowired private TableRepository tableRepo;
    @Autowired private MenuItemRepository menuItemRepo;

    // ==========================================
    // HOA DON (ORDERS)
    // ==========================================

    /** BUOC 2: Nhan vien mo ban -> Tao hoa don moi, danh dau ban co khach */
    @PostMapping("/open")
    public ResponseEntity<?> openTable(@RequestBody Map<String, Integer> data) {
        Integer tableId = data.get("tableId");

        RestaurantTable table = tableRepo.findById(tableId).orElse(null);
        if (table == null) return ResponseEntity.badRequest()
                .body(Map.of("message", "Khong tim thay ban!"));

        if (table.getIsOccupied()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Ban nay dang co khach!"));
        }

        // Tao hoa don moi
        Order order = new Order();
        order.setTable(table);
        order.setStatus(0);
        order.setTotalAmount(BigDecimal.ZERO);
        Order savedOrder = orderRepo.save(order);

        // Cap nhat trang thai ban -> co khach
        table.setIsOccupied(true);
        tableRepo.save(table);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }

    /** Lay hoa don dang phuc vu (status=0) cua 1 ban */
    @GetMapping("/table/{tableId}/active")
    public ResponseEntity<?> getActiveOrder(@PathVariable Integer tableId) {
        return orderRepo.findByTableIdAndStatus(tableId, 0)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Lay thong tin hoa don */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Integer id) {
        return orderRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // CHI TIET GOI MON (ORDER_DETAILS)
    // ==========================================

    /** BUOC 3: Nhan vien them mon vao gio (luu RAM -> status=0 la nhap) */
    @PostMapping("/{orderId}/items")
    public ResponseEntity<?> addItem(@PathVariable Integer orderId,
                                     @RequestBody Map<String, Object> data) {
        Integer itemId   = (Integer) data.get("itemId");
        Integer quantity = (Integer) data.get("quantity");
        String  note     = (String)  data.get("note");

        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null) return ResponseEntity.badRequest()
                .body(Map.of("message", "Khong tim thay hoa don!"));
        if (order.getStatus() != 0) return ResponseEntity.badRequest()
                .body(Map.of("message", "Hoa don da dong!"));

        MenuItem menuItem = menuItemRepo.findById(itemId).orElse(null);
        if (menuItem == null) return ResponseEntity.badRequest()
                .body(Map.of("message", "Khong tim thay mon an!"));

        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);
        detail.setMenuItem(menuItem);
        detail.setQuantity(quantity);
        detail.setPriceAtSale(menuItem.getPrice()); // Chot gia ngay khi goi
        detail.setNote(note);
        detail.setStatus(0); // Nhap - nhan vien con co the sua

        return ResponseEntity.status(HttpStatus.CREATED).body(detailRepo.save(detail));
    }

    /** Lay tat ca mon trong 1 hoa don */
    @GetMapping("/{orderId}/details")
    public List<OrderDetail> getDetails(@PathVariable Integer orderId) {
        return detailRepo.findByOrderId(orderId);
    }

    /** BUOC 4: Nhan vien gui mon -> Chuyen tat ca mon nhap (status=0) sang da gui (status=1) - KHOA */
    @PutMapping("/{orderId}/send")
    public ResponseEntity<?> sendOrder(@PathVariable Integer orderId) {
        List<OrderDetail> draftItems = detailRepo.findByOrderIdAndStatus(orderId, 0);
        if (draftItems.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Khong co mon nao trong gio!"));
        }
        // Chuyen tat ca nhap -> da gui bep
        draftItems.forEach(d -> d.setStatus(1));
        detailRepo.saveAll(draftItems);

        return ResponseEntity.ok(Map.of(
                "message", "Da gui " + draftItems.size() + " mon len bep!",
                "sentCount", draftItems.size()
        ));
    }

    /** BUOC 5: QUAN LY huy mon da gui (status 1 -> 2) */
    @PutMapping("/details/{detailId}/cancel")
    public ResponseEntity<?> cancelItem(@PathVariable Integer detailId,
                                        @RequestBody Map<String, Integer> data) {
        // role phai la 1 (Quan ly) - kiem tra ben phia Android hoac them JWT sau
        return detailRepo.findById(detailId).map(detail -> {
            if (detail.getStatus() == 2) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Mon nay da bi huy roi!"));
            }
            detail.setStatus(2); // Huy - giu lai de doi soat cuoi thang
            detailRepo.save(detail);
            return ResponseEntity.ok(Map.of("message", "Da huy mon! Giu lai de doi soat."));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** BUOC 6: Thanh toan -> Tinh tong, dong hoa don, giai phong ban */
    @PostMapping("/{orderId}/checkout")
    public ResponseEntity<?> checkout(@PathVariable Integer orderId) {
        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null) return ResponseEntity.notFound().build();
        if (order.getStatus() != 0) return ResponseEntity.badRequest()
                .body(Map.of("message", "Hoa don da dong!"));

        // Tinh tong: chi cong nhung mon status=1 (da gui bep, khong tinh mon huy)
        BigDecimal total = detailRepo.calculateTotalAmount(orderId);

        // Dong hoa don
        order.setTotalAmount(total);
        order.setStatus(1); // Da thanh toan
        orderRepo.save(order);

        // Giai phong ban -> trang thai trong
        RestaurantTable table = order.getTable();
        table.setIsOccupied(false);
        tableRepo.save(table);

        return ResponseEntity.ok(Map.of(
                "message", "Thanh toan thanh cong!",
                "totalAmount", total,
                "orderId", orderId
        ));
    }
}
