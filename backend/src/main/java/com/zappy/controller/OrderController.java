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

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * ============================================================
 *  ORDER CONTROLLER - TRUNG TÂM XỬ LÝ NGHIỆP VỤ GỌI MÓN
 * ============================================================
 *
 * Đây là Controller chính xử lý toàn bộ luồng phục vụ khách:
 *
 *  LUỒNG CHÍNH (theo thứ tự):
 *  ┌─────────────────────────────────────────────────────────┐
 *  │ BƯỚC 1: Khách đến → Nhân viên chọn bàn trống            │
 *  │ BƯỚC 2: POST /api/orders/open → Tạo hóa đơn mới        │
 *  │ BƯỚC 3: POST /api/orders/{id}/items → Thêm món vào giỏ  │
 *  │ BƯỚC 4: PUT  /api/orders/{id}/send → Gửi món lên bếp    │
 *  │ BƯỚC 5: (Quản lý) PUT /details/{id}/cancel → Hủy món    │
 *  │ BƯỚC 6: POST /api/orders/{id}/checkout → Thanh toán     │
 *  └─────────────────────────────────────────────────────────┘
 *
 *  CÁC API KHÁC:
 *  GET  /api/orders/{id}                    → Lấy thông tin hóa đơn
 *  GET  /api/orders/table/{tableId}/active  → Hóa đơn đang phục vụ của 1 bàn
 *  GET  /api/orders/restaurant/{resId}/active → Tất cả hóa đơn đang phục vụ
 *  GET  /api/orders/{id}/details            → Danh sách món trong hóa đơn
 *  PUT  /api/orders/details/{id}/cancel     → Quản lý hủy món đã gửi bếp
 *  GET  /api/orders/stats/restaurant/{id}   → Thống kê doanh thu
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    // Spring tự động inject (tiêm) các repository vào để sử dụng
    @Autowired private OrderRepository orderRepo;           // Thao tác bảng orders
    @Autowired private OrderDetailRepository detailRepo;   // Thao tác bảng order_details
    @Autowired private TableRepository tableRepo;          // Thao tác bảng tables
    @Autowired private MenuItemRepository menuItemRepo;    // Thao tác bảng menu_items

    // ==========================================
    // PHẦN 1: QUẢN LÝ HÓA ĐƠN (ORDERS)
    // ==========================================

    /**
     * [BƯỚC 2] MỞ BÀN - Nhân viên tạo hóa đơn mới khi có khách ngồi vào bàn
     *
     * Luồng xử lý:
     *  1. Nhận tableId (mã bàn) và userId (mã nhân viên) từ app Android
     *  2. Kiểm tra bàn có tồn tại không
     *  3. Kiểm tra bàn có đang trống không (nếu đang có khách thì báo lỗi)
     *  4. Tạo đối tượng Order mới với status = 0 (đang phục vụ)
     *  5. Lưu hóa đơn vào DB
     *  6. Cập nhật trạng thái bàn → isOccupied = true (bàn có khách)
     *  7. Trả về hóa đơn vừa tạo cho app Android
     *
     * @param data JSON body gồm: { "tableId": 1, "userId": 5 }
     * @return Hóa đơn mới tạo (HTTP 201) hoặc lỗi (HTTP 400)
     *
     * Ví dụ gọi từ Android:
     *   POST /api/orders/open
     *   Body: { "tableId": 3, "userId": 7 }
     */
    @PostMapping("/open")
    public ResponseEntity<?> openTable(@RequestBody Map<String, Integer> data) {
        // Lấy mã bàn và mã nhân viên từ body request
        Integer tableId = data.get("tableId");
        Integer userId = data.get("userId"); // Nhân viên tạo order

        // Bước 2a: Tìm bàn trong CSDL theo ID
        // → Nếu không tìm thấy, trả về lỗi 400 Bad Request
        RestaurantTable table = tableRepo.findById(tableId).orElse(null);
        if (table == null) return ResponseEntity.badRequest()
                .body(Map.of("message", "Không tìm thấy bàn!"));

        // Bước 2b: Kiểm tra bàn có đang bận không
        // → isOccupied = true nghĩa là bàn đang có khách, không thể mở thêm
        if (table.getIsOccupied()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Bàn này đang có khách!"));
        }

        // Bước 2c: Tạo đối tượng User chỉ với ID (không cần load toàn bộ thông tin)
        // → Dùng để gán nhân viên phụ trách vào hóa đơn
        com.zappy.entity.User user = null;
        if (userId != null) {
            user = new com.zappy.entity.User();
            user.setId(userId);
        }

        // Bước 2d: Tạo hóa đơn mới
        Order order = new Order();
        order.setTable(table);               // Gán bàn cho hóa đơn
        order.setUser(user);                 // Gán nhân viên phụ trách
        order.setStatus(0);                  // 0 = Đang phục vụ (chưa thanh toán)
        order.setTotalAmount(BigDecimal.ZERO); // Tổng tiền ban đầu = 0
        Order savedOrder = orderRepo.save(order); // Lưu vào DB, createdAt tự sinh (@PrePersist)

        // Bước 2e: Đánh dấu bàn là "có khách" → app hiển thị màu đỏ
        table.setIsOccupied(true);
        tableRepo.save(table);

        // Trả về HTTP 201 Created kèm theo hóa đơn vừa tạo
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }

    /**
     * LẤY HÓA ĐƠN ĐANG PHỤC VỤ CỦA 1 BÀN
     *
     * Dùng khi nhân viên bấm vào bàn đang đỏ → muốn xem hóa đơn hiện tại
     *
     * Luồng xử lý:
     *  1. Tìm hóa đơn có tableId = {tableId} VÀ status = 0 (đang phục vụ)
     *  2. Nếu tìm thấy → trả về hóa đơn
     *  3. Nếu không thấy → trả về 404 Not Found
     *
     * @param tableId ID của bàn cần tìm hóa đơn
     * @return Hóa đơn đang phục vụ (HTTP 200) hoặc 404
     */
    @GetMapping("/table/{tableId}/active")
    public ResponseEntity<?> getActiveOrder(@PathVariable Integer tableId) {
        // findByTableIdAndStatus: Spring JPA tự generate câu SQL tương đương:
        // SELECT * FROM orders WHERE table_id = ? AND status = 0 LIMIT 1
        return orderRepo.findByTableIdAndStatus(tableId, 0)
                .map(ResponseEntity::ok)          // Tìm thấy → 200 OK
                .orElse(ResponseEntity.notFound().build()); // Không thấy → 404
    }

    /**
     * LẤY TẤT CẢ HÓA ĐƠN ĐANG PHỤC VỤ CỦA TOÀN NHÀ HÀNG
     *
     * Dùng cho màn hình "Danh sách order đang phục vụ" - hiển thị tất cả bàn đang bận
     * Sau khi lấy danh sách, tính lại tổng tiền thực tế cho từng hóa đơn
     *
     * Luồng xử lý:
     *  1. Tìm tất cả hóa đơn có status = 0 thuộc nhà hàng resId
     *  2. Với mỗi hóa đơn, tính lại tổng tiền từ bảng order_details
     *     (bởi vì totalAmount trong bảng orders chỉ cập nhật khi checkout)
     *  3. Trả về danh sách
     *
     * @param resId ID của nhà hàng
     * @return Danh sách hóa đơn đang hoạt động
     */
    @GetMapping("/restaurant/{resId}/active")
    public ResponseEntity<?> getActiveOrdersByRestaurant(@PathVariable Integer resId) {
        // Câu JPQL tương đương SQL:
        // SELECT o.* FROM orders o
        //   JOIN tables t ON o.table_id = t.id
        //   JOIN areas a ON t.area_id = a.id
        // WHERE a.restaurant_id = ? AND o.status = 0
        List<Order> activeOrders = orderRepo.findByRestaurantIdAndStatus(resId, 0);

        // Tính lại tổng tiền thực cho từng hóa đơn
        // (tổng = Σ quantity * price_at_sale của các món chưa hủy)
        for (Order order : activeOrders) {
            BigDecimal total = detailRepo.calculateTotalAmount(order.getId());
            if (total != null) {
                order.setTotalAmount(total);
            }
        }
        return ResponseEntity.ok(activeOrders);
    }

    /**
     * LẤY DANH SÁCH HÓA ĐƠN ĐÃ THANH TOÁN (CÓ LỌC THEO NGÀY)
     *
     * Dùng cho màn hình "Lịch sử hóa đơn" - cho phép lọc theo khoảng thời gian
     *
     * Luồng xử lý:
     *  1. Parse fromDate và toDate từ query string (nếu có)
     *     Định dạng: "2024-01-15T00:00:00"
     *  2. Gọi query lọc các hóa đơn status = 1 (đã thanh toán)
     *  3. Sắp xếp mới nhất lên trên (theo checkoutAt, hoặc createdAt nếu không có)
     *  4. Trả về danh sách
     *
     * @param resId    ID nhà hàng
     * @param fromDate Từ ngày (tùy chọn), ví dụ: "2024-01-01T00:00:00"
     * @param toDate   Đến ngày (tùy chọn), ví dụ: "2024-01-31T23:59:59"
     */
    @GetMapping("/restaurant/{resId}/paid")
    public ResponseEntity<?> getPaidOrdersByRestaurant(
            @PathVariable Integer resId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {

        // Bước 1: Chuyển đổi chuỗi ngày thành LocalDateTime để truyền vào query
        java.time.LocalDateTime from = null;
        java.time.LocalDateTime to = null;
        try {
            if (fromDate != null && !fromDate.isEmpty()) from = java.time.LocalDateTime.parse(fromDate);
            if (toDate != null && !toDate.isEmpty()) to = java.time.LocalDateTime.parse(toDate);
        } catch (Exception e) {
            // Nếu định dạng ngày sai → bỏ qua, tìm không lọc ngày
        }

        // Bước 2: Truy vấn CSDL lấy hóa đơn đã thanh toán (status=1), lọc theo ngày nếu có
        List<Order> paidOrders = orderRepo.findPaidOrdersWithFilter(resId, 1, from, to);
        
        // Bước 3: Sắp xếp danh sách - hóa đơn mới nhất lên trên
        // Ưu tiên sắp xếp theo checkoutAt, nếu không có thì dùng createdAt, cuối cùng dùng id
        paidOrders.sort((o1, o2) -> {
            if (o1.getCheckoutAt() != null && o2.getCheckoutAt() != null) {
                return o2.getCheckoutAt().compareTo(o1.getCheckoutAt()); // Mới nhất lên đầu
            }
            if (o1.getCreatedAt() != null && o2.getCreatedAt() != null) {
                return o2.getCreatedAt().compareTo(o1.getCreatedAt());
            }
            // Dự phòng: sắp xếp theo id giảm dần
            return Integer.compare(o2.getId(), o1.getId());
        });
        
        return ResponseEntity.ok(paidOrders);
    }

    /**
     * LẤY THÔNG TIN CHI TIẾT 1 HÓA ĐƠN THEO ID
     *
     * Dùng khi cần xem thông tin tổng quát của 1 hóa đơn cụ thể
     * (bàn số mấy, nhân viên nào phụ trách, trạng thái, tổng tiền...)
     *
     * @param id ID của hóa đơn
     * @return Hóa đơn (HTTP 200) hoặc 404 nếu không tìm thấy
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Integer id) {
        // Spring JPA tự generate: SELECT * FROM orders WHERE id = ?
        return orderRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // PHẦN 2: CHI TIẾT GỌI MÓN (ORDER_DETAILS)
    // ==========================================

    /**
     * [BƯỚC 3] THÊM 1 MÓN VÀO HÓA ĐƠN
     *
     * Nhân viên chọn 1 món từ menu và thêm vào giỏ hàng của bàn.
     * Món mới thêm có status = 0 (nhập, chưa gửi bếp) → nhân viên còn có thể xóa.
     *
     * Luồng xử lý:
     *  1. Kiểm tra hóa đơn tồn tại và đang mở (status = 0)
     *  2. Kiểm tra món ăn tồn tại trong menu
     *  3. Tạo OrderDetail mới với giá được chốt tại thời điểm gọi (priceAtSale)
     *     → Điều này giúp tránh thay đổi giá nếu sau này menu bị chỉnh sửa
     *  4. Lưu và trả về chi tiết vừa thêm
     *
     * @param orderId ID của hóa đơn
     * @param data    JSON body: { "itemId": 5, "quantity": 2, "note": "ít cay" }
     * @return OrderDetail vừa tạo (HTTP 201)
     */
    @PostMapping("/{orderId}/items")
    public ResponseEntity<?> addItem(@PathVariable Integer orderId,
                                     @RequestBody Map<String, Object> data) {
        // Lấy thông tin món từ request body
        Integer itemId   = (Integer) data.get("itemId");    // ID món ăn
        Integer quantity = (Integer) data.get("quantity");  // Số lượng
        String  note     = (String)  data.get("note");      // Ghi chú của khách

        // Bước 3a: Tìm hóa đơn - phải tồn tại và đang mở
        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null) return ResponseEntity.badRequest()
                .body(Map.of("message", "Không tìm thấy hóa đơn!"));
        // Nếu hóa đơn đã đóng (status != 0) → không được thêm món
        if (order.getStatus() != 0) return ResponseEntity.badRequest()
                .body(Map.of("message", "Hóa đơn đã đóng!"));

        // Bước 3b: Tìm món ăn trong menu
        MenuItem menuItem = menuItemRepo.findById(itemId).orElse(null);
        if (menuItem == null) return ResponseEntity.badRequest()
                .body(Map.of("message", "Không tìm thấy món ăn!"));

        // Bước 3c: Tạo chi tiết order
        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);                        // Thuộc hóa đơn nào
        detail.setMenuItem(menuItem);                  // Món ăn nào
        detail.setQuantity(quantity);                  // Số lượng
        detail.setPriceAtSale(menuItem.getPrice());    // Chốt giá ngay lúc gọi (quan trọng!)
        detail.setNote(note);                          // Ghi chú đặc biệt của khách
        detail.setStatus(0);                           // status = 0: Nhập, chưa gửi bếp

        // Bước 3d: Lưu vào DB và trả về kết quả
        return ResponseEntity.status(HttpStatus.CREATED).body(detailRepo.save(detail));
    }

    /**
     * [BƯỚC 3 - MỞ RỘNG] THÊM NHIỀU MÓN CÙNG LÚC (BATCH)
     *
     * Tối ưu hơn so với gọi addItem nhiều lần.
     * App Android gom tất cả món đã chọn rồi gửi 1 lần duy nhất.
     *
     * Luồng xử lý:
     *  1. Kiểm tra hóa đơn hợp lệ
     *  2. Duyệt qua từng món trong danh sách:
     *     - Nếu món tồn tại và số lượng > 0 → tạo OrderDetail
     *     - Bỏ qua món không hợp lệ (không báo lỗi, tiếp tục xử lý)
     *  3. Lưu tất cả OrderDetail một lần bằng saveAll() → hiệu quả hơn
     *
     * @param orderId   ID hóa đơn
     * @param itemsData Danh sách món, mỗi món là: { "itemId": X, "quantity": Y, "note": "..." }
     */
    @PostMapping("/{orderId}/items/batch")
    public ResponseEntity<?> addBatchItems(@PathVariable Integer orderId,
                                           @RequestBody List<Map<String, Object>> itemsData) {
        // Kiểm tra hóa đơn tồn tại và đang mở
        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null) return ResponseEntity.badRequest()
                .body(Map.of("message", "Không tìm thấy hóa đơn!"));
        if (order.getStatus() != 0) return ResponseEntity.badRequest()
                .body(Map.of("message", "Hóa đơn đã đóng!"));

        // Danh sách chi tiết cần lưu
        List<OrderDetail> detailsToSave = new java.util.ArrayList<>();

        // Duyệt từng món trong request
        for (Map<String, Object> data : itemsData) {
            Integer itemId   = (Integer) data.get("itemId");
            Integer quantity = (Integer) data.get("quantity");
            String  note     = (String)  data.get("note");

            // Chỉ xử lý nếu món tồn tại và số lượng hợp lệ (> 0)
            MenuItem menuItem = menuItemRepo.findById(itemId).orElse(null);
            if (menuItem != null && quantity != null && quantity > 0) {
                OrderDetail detail = new OrderDetail();
                detail.setOrder(order);
                detail.setMenuItem(menuItem);
                detail.setQuantity(quantity);
                detail.setPriceAtSale(menuItem.getPrice()); // Chốt giá lúc gọi
                detail.setNote(note);
                detail.setStatus(0); // Nhập, chưa gửi bếp
                detailsToSave.add(detail);
            }
        }

        // Lưu tất cả trong 1 lần gọi DB → hiệu quả hơn nhiều lần save()
        detailRepo.saveAll(detailsToSave);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Thêm thành công " + detailsToSave.size() + " món"));
    }

    /**
     * XEM DANH SÁCH MÓN ĐÃ ORDER CỦA 1 HÓA ĐƠN
     *
     * Trả về toàn bộ danh sách món (cả status 0, 1, 2) trong hóa đơn.
     * App Android dùng để hiển thị:
     *  - Màu xám: status = 0 (nhập, chưa gửi)
     *  - Màu xanh: status = 1 (đã gửi bếp)
     *  - Màu đỏ gạch: status = 2 (đã hủy)
     *
     * @param orderId ID của hóa đơn
     * @return Danh sách OrderDetail (tất cả trạng thái)
     */
    @GetMapping("/{orderId}/details")
    public List<OrderDetail> getDetails(@PathVariable Integer orderId) {
        // Spring JPA tự generate: SELECT * FROM order_details WHERE order_id = ?
        return detailRepo.findByOrderId(orderId);
    }

    /**
     * [BƯỚC 4] GỬI MÓN LÊN BẾP - Khóa các món đang nhập
     *
     * Sau khi nhân viên đã thêm đủ món, bấm "Gửi bếp" → hệ thống chuyển
     * tất cả món có status = 0 (nhập) sang status = 1 (đã gửi bếp - KHÓA).
     *
     * Sau bước này, nhân viên KHÔNG THỂ xóa món nữa.
     * Chỉ quản lý mới có thể hủy món đã gửi.
     *
     * Luồng xử lý:
     *  1. Tìm tất cả món có status = 0 trong hóa đơn
     *  2. Nếu giỏ trống → báo lỗi
     *  3. Chuyển tất cả status 0 → 1 (đã gửi bếp)
     *  4. Lưu vào DB và trả về kết quả
     *
     * @param orderId ID hóa đơn
     * @return Thông báo số món đã gửi
     */
    @PutMapping("/{orderId}/send")
    public ResponseEntity<?> sendOrder(@PathVariable Integer orderId) {
        // Bước 4a: Tìm tất cả món đang ở trạng thái "nhập" (status = 0)
        // SQL tương đương: SELECT * FROM order_details WHERE order_id = ? AND status = 0
        List<OrderDetail> draftItems = detailRepo.findByOrderIdAndStatus(orderId, 0);

        // Nếu không có món nào chưa gửi → báo lỗi
        if (draftItems.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Không có món nào trong giỏ!"));
        }

        // Bước 4b: Chuyển tất cả món nhập → đã gửi bếp (KHÓA)
        // forEach duyệt qua danh sách và set status = 1 cho từng món
        draftItems.forEach(d -> d.setStatus(1));

        // Bước 4c: Lưu tất cả thay đổi vào DB trong 1 lần gọi
        detailRepo.saveAll(draftItems);

        return ResponseEntity.ok(Map.of(
                "message", "Đã gửi " + draftItems.size() + " món lên bếp!",
                "sentCount", draftItems.size()
        ));
    }

    /**
     * [BƯỚC 5 - QUẢN LÝ] HỦY MÓN ĐÃ GỬI BẾP
     *
     * Chỉ quản lý (role = 1) mới được phép hủy món đã gửi bếp.
     * Nhân viên thường chỉ xóa được món chưa gửi (status = 0).
     *
     * Lưu ý: Món hủy vẫn được GIỮ LẠI trong DB (status = 2),
     * không xóa hẳn → để đối soát cuối tháng, tránh gian lận.
     *
     * Luồng xử lý:
     *  1. Tìm OrderDetail theo detailId
     *  2. Nếu món đã hủy rồi → báo lỗi
     *  3. Đánh dấu status = 2 (đã hủy)
     *  4. Lưu lý do hủy (nếu có)
     *  5. Cập nhật updatedAt = thời điểm hủy
     *  6. Lưu vào DB
     *
     * @param detailId     ID của chi tiết món cần hủy
     * @param cancelReason Lý do hủy (tùy chọn, truyền qua query param)
     * @param data         Body (tùy chọn, không bắt buộc)
     */
    @PutMapping("/details/{detailId}/cancel")
    public ResponseEntity<?> cancelItem(@PathVariable Integer detailId,
                                        @RequestParam(value = "cancelReason", required = false) String cancelReason,
                                        @RequestBody(required = false) Map<String, Integer> data) {
        // Lưu ý: Việc kiểm tra role (quản lý hay nhân viên) hiện được thực hiện
        // bên phía app Android. Trong tương lai nên thêm JWT để bảo mật server-side.
        return detailRepo.findById(detailId).map(detail -> {
            // Kiểm tra: Nếu món đã bị hủy trước đó → không hủy lại
            if (detail.getStatus() == 2) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Món này đã bị hủy rồi!"));
            }

            // Chuyển trạng thái sang đã hủy (giữ lại để đối soát)
            detail.setStatus(2);

            // Lưu lý do hủy nếu có cung cấp
            if (cancelReason != null && !cancelReason.trim().isEmpty()) {
                detail.setCancelReason(cancelReason.trim());
            }

            // Ghi lại thời điểm hủy
            detail.setUpdatedAt(java.time.LocalDateTime.now());
            detailRepo.save(detail);

            return ResponseEntity.ok(Map.of("message", "Đã hủy món! Giữ lại để đối soát."));
        }).orElse(ResponseEntity.notFound().build()); // Nếu không tìm thấy detailId → 404
    }
    
    /**
     * LẤY DANH SÁCH MÓN ĐÃ HỦY CỦA NHÀ HÀNG (CÓ LỌC THEO NGÀY)
     *
     * Dùng cho màn hình báo cáo hủy món của quản lý.
     * Mặc định lấy hôm nay nếu không truyền fromDate/toDate.
     *
     * @param resId    ID nhà hàng
     * @param fromDate Từ ngày (tùy chọn)
     * @param toDate   Đến ngày (tùy chọn)
     */
    @GetMapping("/restaurant/{resId}/cancelled")
    public ResponseEntity<?> getCancelledOrdersByRestaurant(
            @PathVariable Integer resId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {

        // Parse chuỗi ngày thành LocalDateTime
        java.time.LocalDateTime from = null;
        java.time.LocalDateTime to = null;
        try {
            if (fromDate != null && !fromDate.isEmpty()) from = java.time.LocalDateTime.parse(fromDate);
            if (toDate != null && !toDate.isEmpty()) to = java.time.LocalDateTime.parse(toDate);
        } catch (Exception e) {
            // Bỏ qua lỗi parse, dùng giá trị mặc định bên dưới
        }

        // Nếu không truyền từ ngày → mặc định lấy từ đầu ngày hôm nay (00:00:00)
        if (from == null) {
            from = java.time.LocalDate.now().atStartOfDay();
        }
        // Nếu không truyền đến ngày → mặc định lấy đến cuối ngày hôm nay (23:59:59)
        if (to == null) {
            to = java.time.LocalDate.now().atTime(23, 59, 59);
        }

        // Lấy danh sách món đã hủy (status = 2) trong khoảng thời gian
        List<OrderDetail> cancelledItems = detailRepo.findCancelledItems(resId, from, to);
        return ResponseEntity.ok(cancelledItems);
    }

    /**
     * [BƯỚC 6] THANH TOÁN - Đóng hóa đơn và giải phóng bàn
     *
     * Bước cuối cùng trong luồng phục vụ. Khi khách thanh toán:
     *  1. Tính tổng tiền thực tế (chỉ tính món đã gửi bếp, bỏ qua món hủy)
     *  2. Cập nhật hóa đơn: status = 1, totalAmount, checkoutAt = now()
     *  3. Giải phóng bàn: isOccupied = false → bàn trở về màu xanh trên app
     *
     * Lưu ý: Chỉ tính tiền các món status = 1 (đã gửi bếp).
     * Món status = 0 (nhập chưa gửi) và status = 2 (hủy) KHÔNG được tính tiền.
     *
     * @param orderId ID hóa đơn cần thanh toán
     * @return Tổng tiền và thông báo thành công
     */
    @PostMapping("/{orderId}/checkout")
    public ResponseEntity<?> checkout(@PathVariable Integer orderId) {
        // Bước 6a: Tìm hóa đơn và kiểm tra trạng thái
        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null) return ResponseEntity.notFound().build();
        // Không cho thanh toán hóa đơn đã đóng
        if (order.getStatus() != 0) return ResponseEntity.badRequest()
                .body(Map.of("message", "Hóa đơn đã đóng!"));

        // Bước 6b: Tính tổng tiền thực thu
        // SQL tương đương: SELECT SUM(quantity * price_at_sale)
        //                  FROM order_details
        //                  WHERE order_id = ? AND status != 2  (loại trừ món hủy)
        BigDecimal total = detailRepo.calculateTotalAmount(orderId);

        // Bước 6c: Đóng hóa đơn
        order.setTotalAmount(total);                              // Lưu tổng tiền
        order.setStatus(1);                                       // 1 = Đã thanh toán
        order.setCheckoutAt(java.time.LocalDateTime.now());       // Ghi thời điểm thanh toán
        orderRepo.save(order);

        // Bước 6d: Giải phóng bàn → app hiển thị màu xanh (bàn trống)
        RestaurantTable table = order.getTable();
        table.setIsOccupied(false);
        tableRepo.save(table);

        return ResponseEntity.ok(Map.of(
                "message", "Thanh toán thành công!",
                "totalAmount", total,
                "orderId", orderId
        ));
    }

    // ==========================================
    // PHẦN 3: THỐNG KÊ DOANH THU (DASHBOARD)
    // ==========================================

    /**
     * THỐNG KÊ TỔNG QUAN - Dashboard cho Quản lý
     *
     * Trả về các chỉ số tổng hợp theo kỳ thời gian:
     *  - totalRevenue:   Tổng doanh thu (tổng tiền các hóa đơn đã thanh toán)
     *  - totalOrders:    Số hóa đơn đã thanh toán
     *  - cancelledItems: Số lượng món đã hủy
     *  - unpaidTables:   Số bàn đang có khách (chưa thanh toán)
     *
     * Các kỳ thống kê hỗ trợ (truyền qua ?period=...):
     *  - "today" (mặc định): Hôm nay từ 00:00:00 đến 23:59:59
     *  - "week":  Từ thứ Hai đến Chủ Nhật tuần hiện tại
     *  - "month": Từ ngày 1 đến hết tháng hiện tại
     *
     * Ví dụ: GET /api/orders/stats/restaurant/1?period=week
     *
     * @param resId  ID nhà hàng
     * @param period Kỳ thống kê: "today" / "week" / "month"
     */
    @GetMapping("/stats/restaurant/{resId}")
    public ResponseEntity<?> getDashboardStats(
            @PathVariable Integer resId,
            @RequestParam(defaultValue = "today") String period) {

        // Khai báo khoảng thời gian thống kê
        LocalDateTime startDate;
        LocalDateTime endDate;
        LocalDate today = LocalDate.now();

        // Xác định khoảng thời gian dựa trên tham số period
        switch (period.toLowerCase()) {

            case "week":
                // Từ đầu thứ Hai → hết 7 ngày (Chủ Nhật)
                startDate = today.with(DayOfWeek.MONDAY).atStartOfDay();
                endDate = startDate.plusWeeks(1);
                break;

            case "month":
                // Từ ngày 1 của tháng hiện tại → hết 1 tháng
                startDate = today.withDayOfMonth(1).atStartOfDay();
                endDate = startDate.plusMonths(1);
                break;

            case "today":
            default:
                // Hôm nay từ 00:00:00 đến 23:59:59 (plusDays(1) vì BETWEEN thường dùng <)
                startDate = today.atStartOfDay();
                endDate = startDate.plusDays(1);
                break;
        }

        // Truy vấn 1: Tổng doanh thu (SUM totalAmount của orders đã thanh toán trong kỳ)
        // JPQL: SELECT COALESCE(SUM(o.totalAmount),0) FROM Order o
        //       WHERE o.table.area.restaurant.id = :resId AND o.status = 1
        //       AND COALESCE(o.checkoutAt, o.createdAt) BETWEEN :startDate AND :endDate
        BigDecimal revenue = orderRepo.getRevenue(resId, startDate, endDate);

        // Truy vấn 2: Số hóa đơn đã thanh toán trong kỳ
        // JPQL: SELECT COUNT(o) FROM Order o WHERE ... (tương tự query trên nhưng COUNT)
        Long totalOrders = orderRepo.countOrders(resId, startDate, endDate);

        // Truy vấn 3: Tổng số lượng món đã hủy trong kỳ
        // JPQL: SELECT COALESCE(SUM(od.quantity),0) FROM OrderDetail od
        //       WHERE od.order.table.area.restaurant.id = :resId AND od.status = 2
        //       AND od.order.createdAt BETWEEN :startDate AND :endDate
        Long cancelledItems = detailRepo.countCancelledItems(resId, startDate, endDate);

        // Truy vấn 4: Số bàn đang có khách (không phụ thuộc kỳ thống kê)
        // JPQL: SELECT COUNT(t) FROM RestaurantTable t
        //       WHERE t.area.restaurant.id = :resId AND t.isOccupied = true
        Long unpaidTables = tableRepo.countOccupiedTables(resId);

        // Đóng gói kết quả trả về
        Map<String, Object> result = new HashMap<>();
        result.put("totalRevenue", revenue);       // Tổng doanh thu
        result.put("totalOrders", totalOrders);    // Số đơn đã thanh toán
        result.put("cancelledItems", cancelledItems); // Số món đã hủy
        result.put("unpaidTables", unpaidTables);  // Số bàn đang phục vụ

        return ResponseEntity.ok(result);
    }
}
