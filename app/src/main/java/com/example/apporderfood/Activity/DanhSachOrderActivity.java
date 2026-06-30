package com.example.apporderfood.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.R;
import com.example.apporderfood.adapter.OrderListAdapter;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.TableModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ============================================================
 *  DANH SÁCH ORDER ACTIVITY - XEM TẤT CẢ HÓA ĐƠN ĐANG PHỤC VỤ
 * ============================================================
 *
 * Màn hình này hiển thị danh sách tất cả bàn ĐANG CÓ KHÁCH (status = 0)
 * của toàn nhà hàng, được lọc theo 2 tab:
 *
 *   Tab "Tất cả (N)":        Tất cả hóa đơn đang phục vụ trong nhà hàng
 *   Tab "Đang phục vụ (N)":  Chỉ các hóa đơn do nhân viên ĐANG LOGIN phụ trách
 *                             → Lọc theo userId được lưu trong SharedPreferences
 *
 * Ngoài ra còn có:
 *   - Ô tìm kiếm (etSearch): Lọc theo tên bàn (tên bàn chứa chuỗi tìm kiếm)
 *   - RecyclerView: Hiển thị danh sách order dạng card
 *
 * Luồng dữ liệu:
 *   SharedPreferences → resId, userId (đã login)
 *   API (GET /api/orders/restaurant/{resId}/active) → allOrders (toàn bộ)
 *   filterList() → danh sách sau khi lọc tab + tìm kiếm → adapter hiển thị
 *
 * Bấm vào 1 order → chuyển sang ChiTietBanActivity
 */
public class DanhSachOrderActivity extends AppCompatActivity {

    // ---- Thanh điều hướng dưới cùng ----
    private LinearLayout navOrder, navSoDo, navTienIch;

    // ---- Tab lọc ----
    private TextView tabTatCa;       // Tab "Tất cả" - hiển thị mọi order đang phục vụ
    private TextView tabDangPhucVu;  // Tab "Đang phục vụ" - chỉ order của nhân viên hiện tại

    // ---- Trạng thái màn hình ----
    private TextView tvEmpty;        // Thông báo "Không có dữ liệu" khi danh sách rỗng
    private RecyclerView rvOrderList; // Danh sách order
    private ProgressBar progressBar; // Vòng loading khi đang gọi API
    private EditText etSearch;       // Ô tìm kiếm theo tên bàn

    // ---- Adapter và dữ liệu ----
    private OrderListAdapter adapter;   // Adapter hiển thị từng order trong RecyclerView

    // Thông tin nhà hàng và nhân viên (đọc từ SharedPreferences sau khi login)
    private int resId = -1;          // ID nhà hàng
    private int currentUserId = -1; // ID nhân viên đang login

    // Trạng thái hiện tại của bộ lọc
    private int currentTab = 0;                  // 0 = Tất cả, 1 = Đang phục vụ (của mình)
    private String currentSearchText = "";       // Văn bản đang tìm kiếm

    // Dữ liệu gốc từ API (không bao giờ bị filter trực tiếp)
    private List<java.util.Map<String, Object>> allOrders = new ArrayList<>();

    private ZappyApiService apiService; // Interface gọi API Retrofit

    // ============================================================
    //  VÒNG ĐỜI ACTIVITY
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Bật chế độ màn hình tràn viền (edge-to-edge)
        setContentView(R.layout.activity_danh_sach_order);

        // Xử lý padding để tránh nội dung bị che bởi thanh trạng thái và thanh điều hướng hệ thống
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Khởi tạo Retrofit service để gọi API
        apiService = RetrofitClient.getApiService();

        // Đọc thông tin đã lưu khi đăng nhập từ SharedPreferences
        // "ZappySession" là tên file SharedPreferences được tạo sau khi login thành công
        SharedPreferences prefs = getSharedPreferences("ZappySession", Context.MODE_PRIVATE);
        resId         = prefs.getInt("RES_ID", -1);   // ID nhà hàng, -1 nếu chưa set
        currentUserId = prefs.getInt("USER_ID", -1);  // ID nhân viên, -1 nếu chưa set

        initViews();          // Bước 1: Ánh xạ view
        setupRecyclerView();  // Bước 2: Thiết lập RecyclerView
        setupClickListeners(); // Bước 3: Gán sự kiện click

        loadTables(); // Bước 4: Gọi API lấy danh sách order
    }

    // ============================================================
    //  BƯỚC 1: ÁNH XẠ VIEW
    // ============================================================

    /**
     * Ánh xạ tất cả View từ file layout XML vào biến Java.
     */
    private void initViews() {
        navOrder   = findViewById(R.id.navOrder);
        navSoDo    = findViewById(R.id.navSoDo);
        navTienIch = findViewById(R.id.navTienIch);

        tabTatCa      = findViewById(R.id.tabTatCa);
        tabDangPhucVu = findViewById(R.id.tabDangPhucVu);
        tvEmpty       = findViewById(R.id.tvEmpty);
        rvOrderList   = findViewById(R.id.rvOrderList);
        progressBar   = findViewById(R.id.progressBar);
        etSearch      = findViewById(R.id.etSearch);
    }

    // ============================================================
    //  BƯỚC 2: THIẾT LẬP RECYCLERVIEW
    // ============================================================

    /**
     * Tạo adapter và gán vào RecyclerView.
     *
     * Khi người dùng bấm vào 1 order trong danh sách:
     *   1. Lấy thông tin bàn từ order (table → area → areaName + tableName)
     *   2. Tạo title hiển thị: "Khu trong - Bàn 05"
     *   3. Chuyển sang ChiTietBanActivity với TABLE_ID, TABLE_NAME, ORDER_ID
     */
    private void setupRecyclerView() {
        adapter = new OrderListAdapter(this, order -> {
            // Lấy thông tin bàn từ order (dữ liệu dạng Map từ JSON)
            java.util.Map<String, Object> table = (java.util.Map<String, Object>) order.get("table");
            if (table != null) {
                Intent intent = new Intent(this, ChiTietBanActivity.class);

                // Lấy tên khu vực (nếu có)
                String areaName = "Không rõ";
                if (table.get("area") != null) {
                    java.util.Map<String, Object> area = (java.util.Map<String, Object>) table.get("area");
                    areaName = (String) area.get("areaName");
                }

                // Tiêu đề hiển thị: "Khu trong - Bàn 05"
                String title = areaName + " - " + table.get("tableName");
                intent.putExtra("TABLE_NAME", title);
                intent.putExtra("TABLE_ID", ((Number) table.get("id")).intValue());

                // Truyền orderId để ChiTietBanActivity không cần gọi thêm API tìm order của bàn
                if (order.get("id") != null) {
                    intent.putExtra("ORDER_ID", ((Number) order.get("id")).intValue());
                }
                startActivity(intent);
            }
        });

        // Sắp xếp theo danh sách dọc (LinearLayoutManager)
        rvOrderList.setLayoutManager(new LinearLayoutManager(this));
        rvOrderList.setAdapter(adapter);
    }

    // ============================================================
    //  BƯỚC 3: SỰ KIỆN CLICK
    // ============================================================

    /**
     * Gán sự kiện click cho:
     *   - Bottom Navigation (navOrder, navSoDo, navTienIch)
     *   - Tab lọc (tabTatCa, tabDangPhucVu)
     *   - Ô tìm kiếm (etSearch)
     */
    private void setupClickListeners() {
        // Tab hiện tại → không làm gì
        navOrder.setOnClickListener(v -> { /* Đang ở trang này, không chuyển */ });

        navSoDo.setOnClickListener(v -> {
            startActivity(new Intent(this, SoDobanActivity.class));
            overridePendingTransition(0, 0); // Không animation
            finish(); // Đóng màn hình hiện tại
        });

        navTienIch.setOnClickListener(v -> {
            startActivity(new Intent(this, TienIchActivity.class));
            overridePendingTransition(0, 0);
        });

        // ---- Tab "Tất cả" ----
        tabTatCa.setOnClickListener(v -> {
            currentTab = 0; // Đặt tab hiện tại = 0 (Tất cả)

            // Đổi giao diện: tab này active (nền tối, chữ sáng)
            tabTatCa.setBackgroundResource(R.drawable.bg_tab_active_dark);
            tabTatCa.setTextColor(getResources().getColor(R.color.surface));

            // Tab kia inactive (nền nhạt, chữ tối)
            tabDangPhucVu.setBackgroundResource(R.drawable.bg_tab_inactive);
            tabDangPhucVu.setTextColor(getResources().getColor(R.color.text_primary));

            filterList(); // Áp dụng lại bộ lọc
        });

        // ---- Tab "Đang phục vụ" (của nhân viên đang login) ----
        tabDangPhucVu.setOnClickListener(v -> {
            currentTab = 1; // Đặt tab hiện tại = 1 (Chỉ order của mình)

            // Đổi giao diện: tab này active
            tabDangPhucVu.setBackgroundResource(R.drawable.bg_tab_active_dark);
            tabDangPhucVu.setTextColor(getResources().getColor(R.color.surface));

            // Tab kia inactive
            tabTatCa.setBackgroundResource(R.drawable.bg_tab_inactive);
            tabTatCa.setTextColor(getResources().getColor(R.color.text_primary));

            filterList(); // Áp dụng lại bộ lọc
        });

        // ---- Ô tìm kiếm - lọc theo tên bàn ----
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Mỗi khi văn bản thay đổi → cập nhật currentSearchText và lọc lại
                    currentSearchText = s.toString().trim().toLowerCase();
                    filterList();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    // ============================================================
    //  BƯỚC 4: GỌI API VÀ HIỂN THỊ DỮ LIỆU
    // ============================================================

    /**
     * Gọi API lấy danh sách tất cả hóa đơn đang phục vụ của nhà hàng.
     *
     * API: GET /api/orders/restaurant/{resId}/active
     *
     * Luồng:
     *   1. Hiển thị ProgressBar, ẩn danh sách
     *   2. Gọi API (bất đồng bộ - chạy nền)
     *   3. Nếu thành công → lưu vào allOrders → cập nhật số lượng tab → lọc danh sách
     *   4. Nếu lỗi → hiện Toast thông báo
     */
    private void loadTables() {
        if (resId == -1) return; // Chưa có thông tin nhà hàng → không gọi API

        // Hiển thị trạng thái đang tải
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvOrderList.setVisibility(View.GONE);

        // Gọi API lấy tất cả order đang active (status = 0) của nhà hàng
        apiService.getActiveOrdersByRestaurant(resId).enqueue(
                new Callback<List<java.util.Map<String, Object>>>() {

            @Override
            public void onResponse(Call<List<java.util.Map<String, Object>>> call,
                                   Response<List<java.util.Map<String, Object>>> response) {
                progressBar.setVisibility(View.GONE); // Ẩn loading

                if (response.isSuccessful() && response.body() != null) {
                    allOrders = response.body(); // Lưu toàn bộ data gốc

                    updateTabsCount(); // Cập nhật số lượng hiển thị trên tab
                    filterList();      // Lọc và hiển thị theo tab + search hiện tại
                } else {
                    Toast.makeText(DanhSachOrderActivity.this,
                            "Lỗi lấy danh sách đơn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<java.util.Map<String, Object>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DanhSachOrderActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("DanhSachOrderActivity", "API Call failed", t); // Ghi log để debug
            }
        });
    }

    /**
     * Cập nhật số lượng order hiển thị trên từng tab.
     *
     * Duyệt qua allOrders và đếm:
     *   - countTatCa:       Tất cả order (mọi nhân viên)
     *   - countDangPhucVu:  Chỉ order của currentUserId (nhân viên đang login)
     *
     * Kết quả: "Tất cả (5)" và "Đang phục vụ (2)"
     */
    private void updateTabsCount() {
        int countTatCa = 0;
        int countDangPhucVu = 0;

        for (java.util.Map<String, Object> order : allOrders) {
            countTatCa++; // Đếm tất cả

            // Kiểm tra xem order này có phải do nhân viên hiện tại tạo không
            if (order.get("user") != null) {
                java.util.Map<String, Object> user = (java.util.Map<String, Object>) order.get("user");
                if (user.get("id") != null
                        && ((Number) user.get("id")).intValue() == currentUserId) {
                    countDangPhucVu++; // Order thuộc về nhân viên đang login
                }
            }
        }

        // Cập nhật text tab với số lượng
        tabTatCa.setText("Tất cả (" + countTatCa + ")");
        tabDangPhucVu.setText("Đang phục vụ (" + countDangPhucVu + ")");
    }

    /**
     * Lọc danh sách order theo tab hiện tại + từ khóa tìm kiếm,
     * sau đó cập nhật adapter để hiển thị kết quả lên RecyclerView.
     *
     * Logic lọc (2 điều kiện AND với nhau):
     *
     *   Điều kiện 1 - Lọc theo tab:
     *     currentTab == 0 → Tất cả order đều qua
     *     currentTab == 1 → Chỉ giữ order có user.id == currentUserId
     *
     *   Điều kiện 2 - Lọc theo tìm kiếm:
     *     currentSearchText rỗng → Không lọc thêm
     *     Có text → Chỉ giữ order có tableName chứa chuỗi tìm kiếm (không phân biệt hoa thường)
     *
     * Kết quả:
     *   - Nếu danh sách rỗng → hiện tvEmpty, ẩn RecyclerView
     *   - Nếu có dữ liệu → ẩn tvEmpty, hiện RecyclerView với danh sách đã lọc
     */
    private void filterList() {
        List<java.util.Map<String, Object>> filtered = new ArrayList<>();

        for (java.util.Map<String, Object> order : allOrders) {
            // ---- Kiểm tra điều kiện tab ----
            boolean matchesTab = false;
            if (currentTab == 0) {
                matchesTab = true; // Tab "Tất cả" → mọi order đều pass
            } else if (currentTab == 1) {
                // Tab "Đang phục vụ" → chỉ giữ order của nhân viên hiện tại
                if (order.get("user") != null) {
                    java.util.Map<String, Object> user = (java.util.Map<String, Object>) order.get("user");
                    if (user.get("id") != null
                            && ((Number) user.get("id")).intValue() == currentUserId) {
                        matchesTab = true;
                    }
                }
            }

            // ---- Kiểm tra điều kiện tìm kiếm (chỉ check nếu pass tab) ----
            if (matchesTab) {
                if (currentSearchText.isEmpty()) {
                    // Không có từ khóa → thêm thẳng vào kết quả
                    filtered.add(order);
                } else {
                    // Có từ khóa → lọc theo tên bàn (so sánh không phân biệt hoa thường)
                    java.util.Map<String, Object> table =
                            (java.util.Map<String, Object>) order.get("table");
                    if (table != null && table.get("tableName") != null) {
                        String tblName = table.get("tableName").toString().toLowerCase();
                        if (tblName.contains(currentSearchText)) {
                            filtered.add(order);
                        }
                    }
                }
            }
        }

        // Cập nhật adapter với danh sách đã lọc
        adapter.setOrderList(filtered);

        // Hiển thị/ẩn empty state
        if (filtered.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvOrderList.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvOrderList.setVisibility(View.VISIBLE);
        }
    }
}
