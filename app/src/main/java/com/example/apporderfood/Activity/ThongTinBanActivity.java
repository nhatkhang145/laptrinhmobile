package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.apporderfood.R;

import androidx.appcompat.app.AppCompatActivity;

/**
 * ============================================================
 *  THÔNG TIN BÀN ACTIVITY - POPUP XEM NHANH BÀN ĐANG CÓ KHÁCH
 * ============================================================
 *
 * Màn hình này hiển thị dưới dạng POPUP (dialog overlay) khi nhân viên
 * bấm vào bàn đang đỏ (đang có khách) trên sơ đồ bàn.
 *
 * Thông tin hiển thị:
 *   - Tên bàn (ví dụ: "Khu trong - Bàn 05")
 *   - Tổng tiền tạm tính (tính từ danh sách món, bỏ qua món hủy)
 *   - Thời gian đã phục vụ (ví dụ: "45 phút đã trôi qua")
 *
 * Luồng xử lý:
 *   SoDobanActivity → bấm bàn đỏ → ThongTinBanActivity (popup)
 *                                         ↓ bấm "Xem chi tiết"
 *                                   ChiTietBanActivity
 *
 * Dữ liệu nhận qua Intent:
 *   - TABLE_ID   (int):    ID bàn
 *   - TABLE_NAME (String): Tên bàn để hiển thị
 */
public class ThongTinBanActivity extends AppCompatActivity {

    // ---- Thanh điều hướng dưới cùng ----
    private LinearLayout navOrder;    // Tab "Order" → DanhSachOrderActivity
    private LinearLayout navSoDo;     // Tab "Sơ đồ" → SoDobanActivity
    private LinearLayout navTienIch;  // Tab "Tiện ích" → TienIchActivity

    // ---- Popup overlay ----
    private FrameLayout dimOverlay;   // Vùng tối bên ngoài popup, bấm vào để đóng

    // ---- Các nút trong popup ----
    private LinearLayout btnViewDetail; // Nút "Xem chi tiết" → chuyển sang ChiTietBanActivity
    private TextView btnClose;          // Nút "X" đóng popup

    // ---- Các TextView hiển thị thông tin ----
    private TextView tvPopupTableName;  // Tên bàn (ví dụ: "Bàn 05")
    private TextView tvPopupAmount;     // Tổng tiền tạm tính (ví dụ: "150,000đ")
    private TextView tvPopupTime;       // Thời gian phục vụ (ví dụ: "30 phút đã trôi qua")

    // ID hóa đơn đang phục vụ của bàn này (cần để truyền sang ChiTietBanActivity)
    private int currentOrderId = -1;

    // ============================================================
    //  VÒNG ĐỜI ACTIVITY
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_tin_ban);

        initViews();       // Bước 1: Ánh xạ các view trong layout
        loadTableData();   // Bước 2: Lấy dữ liệu từ Intent và gọi API
        setupClickListeners(); // Bước 3: Gán sự kiện click cho các nút
    }

    // ============================================================
    //  BƯỚC 1: ÁNH XẠ VIEW
    // ============================================================

    /**
     * Ánh xạ tất cả View từ layout XML vào biến Java.
     * Phải gọi TRƯỚC khi sử dụng bất kỳ biến View nào.
     */
    private void initViews() {
        navOrder      = findViewById(R.id.navOrder);
        navSoDo       = findViewById(R.id.navSoDo);
        navTienIch    = findViewById(R.id.navTienIch);
        dimOverlay    = findViewById(R.id.dimOverlay);
        btnViewDetail = findViewById(R.id.btnViewDetail);
        btnClose      = findViewById(R.id.btnClose);
        tvPopupTableName = findViewById(R.id.tvPopupTableName);
        tvPopupAmount = findViewById(R.id.tvPopupAmount);
        tvPopupTime   = findViewById(R.id.tvPopupTime);
    }

    // ============================================================
    //  BƯỚC 2: TẢI DỮ LIỆU
    // ============================================================

    /**
     * Lấy dữ liệu bàn từ Intent (do màn hình trước truyền sang)
     * và hiển thị thông tin ban đầu, sau đó gọi API lấy dữ liệu thực.
     *
     * Luồng:
     *   1. Lấy TABLE_NAME → hiển thị ngay lên tvPopupTableName
     *   2. Hiển thị "Đang tải..." trong khi chờ API
     *   3. Gọi fetchActiveOrder() để lấy thông tin hóa đơn từ server
     */
    private void loadTableData() {
        // Lấy tên bàn từ Intent và hiển thị ngay (không cần chờ API)
        String tableName = getIntent().getStringExtra("TABLE_NAME");
        if (tableName != null && tvPopupTableName != null) {
            tvPopupTableName.setText(tableName);
        }

        // Hiển thị trạng thái "đang tải" trong khi chờ API phản hồi
        tvPopupAmount.setText("...");
        tvPopupTime.setText("Đang tải...");

        // Lấy TABLE_ID từ Intent để gọi API, -1 là giá trị mặc định nếu không có
        int tableId = getIntent().getIntExtra("TABLE_ID", -1);
        if (tableId != -1) {
            fetchActiveOrder(tableId); // Gọi API lấy hóa đơn đang phục vụ
        }
    }

    /**
     * Gọi API lấy hóa đơn đang phục vụ của bàn (status = 0).
     * Sau đó gọi tiếp API lấy danh sách món để tính tổng tiền.
     *
     * Luồng gọi API (2 bước liên tiếp - callback lồng nhau):
     *
     *   Bước A: GET /api/orders/table/{tableId}/active
     *             → Lấy orderId của hóa đơn đang mở
     *             → Lưu vào currentOrderId (dùng sau khi bấm "Xem chi tiết")
     *             → Lấy createdAt để tính thời gian phục vụ
     *
     *   Bước B: GET /api/orders/{orderId}/details  (chạy bên trong callback của Bước A)
     *             → Lấy danh sách món
     *             → Tính tổng tiền: Σ (quantity × priceAtSale) với status ≠ 2 (không tính món hủy)
     *             → Hiển thị lên tvPopupAmount
     *
     * @param tableId ID bàn cần lấy hóa đơn
     */
    private void fetchActiveOrder(int tableId) {
        // ---- Bước A: Gọi API lấy hóa đơn active của bàn ----
        // API: GET /api/orders/table/{tableId}/active
        com.example.apporderfood.api.RetrofitClient.getApiService().getActiveOrder(tableId)
            .enqueue(new retrofit2.Callback<java.util.Map>() {

                @Override
                public void onResponse(retrofit2.Call<java.util.Map> call,
                                       retrofit2.Response<java.util.Map> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        java.util.Map body = response.body();

                        // Lấy ID hóa đơn từ response JSON
                        Object orderIdObj = body.get("id");
                        if (orderIdObj != null) {
                            int orderId = ((Number) orderIdObj).intValue();
                            currentOrderId = orderId; // Lưu để dùng khi bấm "Xem chi tiết"

                            // ---- Bước B: Gọi API lấy danh sách món của hóa đơn ----
                            // API: GET /api/orders/{orderId}/details
                            com.example.apporderfood.api.RetrofitClient.getApiService()
                                .getOrderDetails(orderId)
                                .enqueue(new retrofit2.Callback<java.util.List<com.example.apporderfood.model.OrderDetail>>() {

                                    @Override
                                    public void onResponse(
                                            retrofit2.Call<java.util.List<com.example.apporderfood.model.OrderDetail>> call,
                                            retrofit2.Response<java.util.List<com.example.apporderfood.model.OrderDetail>> detailResponse) {

                                        if (detailResponse.isSuccessful() && detailResponse.body() != null) {
                                            // Tính tổng tiền: cộng tất cả món CHƯA HỦY (status != 2)
                                            double total = 0;
                                            for (com.example.apporderfood.model.OrderDetail d : detailResponse.body()) {
                                                if (d.getStatus() != 2) { // Bỏ qua món đã hủy
                                                    total += d.getQuantity() * d.getPriceAtSale().doubleValue();
                                                }
                                            }
                                            // Định dạng tiền tệ: 150000 → "150,000đ"
                                            java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");
                                            tvPopupAmount.setText(formatter.format(total) + "đ");
                                        } else {
                                            tvPopupAmount.setText("0đ"); // Chưa có món nào
                                        }
                                    }

                                    @Override
                                    public void onFailure(
                                            retrofit2.Call<java.util.List<com.example.apporderfood.model.OrderDetail>> call,
                                            Throwable t) {
                                        tvPopupAmount.setText("Lỗi"); // Lỗi mạng
                                    }
                                });
                        } else {
                            tvPopupAmount.setText("0đ"); // Hóa đơn không có ID (dữ liệu lạ)
                        }

                        // ---- Tính thời gian phục vụ từ createdAt ----
                        if (body.get("createdAt") != null) {
                            String createdAtStr = (String) body.get("createdAt");
                            try {
                                // API LocalDateTime.parse() yêu cầu Android API 26 (Oreo) trở lên
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    java.time.LocalDateTime createdAt = java.time.LocalDateTime.parse(createdAtStr);

                                    // Tính số phút đã trôi qua từ lúc mở bàn đến giờ
                                    long minutes = java.time.temporal.ChronoUnit.MINUTES
                                            .between(createdAt, java.time.LocalDateTime.now());

                                    // Hiển thị: nếu < 60 phút thì hiện phút, ngược lại hiện giờ + phút
                                    if (minutes < 60) {
                                        tvPopupTime.setText(minutes + " phút đã trôi qua");
                                    } else {
                                        long hours = minutes / 60;
                                        long mins  = minutes % 60;
                                        tvPopupTime.setText(hours + " giờ " + mins + " phút đã trôi qua");
                                    }
                                } else {
                                    // Thiết bị cũ hơn Android 8.0 → không tính được
                                    tvPopupTime.setText("Đang phục vụ");
                                }
                            } catch (Exception e) {
                                // Lỗi parse ngày giờ → hiện thông báo chung
                                tvPopupTime.setText("Đang phục vụ");
                            }
                        } else {
                            // Không có createdAt trong response → vừa mới tạo
                            tvPopupTime.setText("Vừa mới mở");
                        }
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<java.util.Map> call, Throwable t) {
                    // Lỗi mạng hoặc server không phản hồi
                    tvPopupAmount.setText("Lỗi");
                    tvPopupTime.setText("...");
                }
            });
    }

    // ============================================================
    //  BƯỚC 3: SỰ KIỆN CLICK
    // ============================================================

    /**
     * Gán sự kiện click cho tất cả các nút trong màn hình.
     */
    private void setupClickListeners() {

        // Bấm vùng tối bên ngoài popup → đóng màn hình (giống bấm ra ngoài dialog)
        dimOverlay.setOnClickListener(v -> finish());

        // Bấm nút "X" góc phải → đóng popup
        btnClose.setOnClickListener(v -> finish());

        // Nút "Xem chi tiết" → chuyển sang ChiTietBanActivity với đầy đủ thông tin
        btnViewDetail.setOnClickListener(v -> {
            if (currentOrderId != -1) {
                // Đã có orderId → chuyển sang màn hình chi tiết
                int tableId = getIntent().getIntExtra("TABLE_ID", -1);
                Intent intent = new Intent(this, ChiTietBanActivity.class);
                intent.putExtra("TABLE_ID", tableId);
                intent.putExtra("TABLE_NAME", getIntent().getStringExtra("TABLE_NAME"));
                intent.putExtra("ORDER_ID", currentOrderId); // Truyền orderId để load chi tiết
                startActivity(intent);
                finish(); // Đóng popup này
            } else {
                // API chưa phản hồi xong → báo chờ
                android.widget.Toast.makeText(this, "Đang tải dữ liệu...",
                        android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        // ---- Điều hướng Bottom Navigation ----

        // Tab "Order" → chuyển sang danh sách order (không đóng màn hình hiện tại)
        navOrder.setOnClickListener(v -> {
            startActivity(new Intent(this, DanhSachOrderActivity.class));
            overridePendingTransition(0, 0); // Không animation chuyển màn hình
        });

        // Tab "Sơ đồ" → về sơ đồ bàn
        navSoDo.setOnClickListener(v -> {
            startActivity(new Intent(this, SoDobanActivity.class));
            overridePendingTransition(0, 0);
            finish(); // Đóng popup hiện tại
        });

        // Tab "Tiện ích" → chuyển sang tiện ích
        navTienIch.setOnClickListener(v -> {
            startActivity(new Intent(this, TienIchActivity.class));
            overridePendingTransition(0, 0);
        });
    }
}
