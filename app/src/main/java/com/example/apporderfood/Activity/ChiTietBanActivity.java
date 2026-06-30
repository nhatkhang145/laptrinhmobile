package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.apporderfood.R;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Map;
import java.util.List;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import android.widget.TextView;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apporderfood.adapter.OrderDetailAdapter;
import com.example.apporderfood.model.OrderDetail;

public class ChiTietBanActivity extends AppCompatActivity {

    private View btnBack;
    private View btnThemMon;
    private View btnTinhTien;
    private View btnGuiBep;

    private TextView tvTitle;
    private TextView tvBadgeCount;
    private TextView tvTotalAmount;
    private RecyclerView rvOrderDetails;
    private OrderDetailAdapter adapter;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    private int orderId = -1;
    private int tableId = -1;
    private String tableName = "";

    /**
     * HÀM KHỞI TẠO (Chạy đầu tiên khi mở màn hình)
     * Nhận ID của Bàn và ID của Order từ màn hình trước truyền sang.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chi_tiet_ban);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        orderId = getIntent().getIntExtra("ORDER_ID", -1);
        tableId = getIntent().getIntExtra("TABLE_ID", -1);
        tableName = getIntent().getStringExtra("TABLE_NAME") != null
                ? getIntent().getStringExtra("TABLE_NAME")
                : "Bàn";

        initViews();
        setupRecyclerView();
        setupClickListeners();
    }

    /**
     * ÁNH XẠ GIAO DIỆN VÀ THIẾT LẬP TIÊU ĐỀ
     */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnThemMon = findViewById(R.id.btnAddItem);
        btnTinhTien = findViewById(R.id.btnTinhTien);
        btnGuiBep = findViewById(R.id.btnGuiBep);

        tvTitle = findViewById(R.id.tvTitle);
        tvBadgeCount = findViewById(R.id.tvBadgeCount);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        rvOrderDetails = findViewById(R.id.rvOrderDetails);

        if (tvTitle != null) {
            tvTitle.setText(tableName + " • Đang phục vụ");
        }

        if (btnThemMon == null) {
            btnThemMon = findViewById(R.id.btnThemMon);
        }
    }

    /**
     * THIẾT LẬP DANH SÁCH (RecyclerView)
     * Kiểm tra quyền của người dùng để quyết định có cho phép hiển thị nút Tính
     * Tiền hay không.
     */
    private void setupRecyclerView() {
        android.content.SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        int userRole = prefs.getInt("ROLE", 0);
        boolean isAdmin = userRole == 1;
        boolean canCheckout = userRole == 1 || userRole == 2;

        if (!canCheckout && btnTinhTien != null) {
            btnTinhTien.setVisibility(View.GONE);
        }

        adapter = new OrderDetailAdapter(this, new java.util.ArrayList<>(), isAdmin, this::cancelItem);
        rvOrderDetails.setLayoutManager(new LinearLayoutManager(this));
        rvOrderDetails.setAdapter(adapter);
        rvOrderDetails.setNestedScrollingEnabled(false);
    }

    /**
     * HÀM HỦY MÓN ĂN (Dành cho Quản lý)
     * Yêu cầu nhập lý do hủy và gọi API báo xuống bếp.
     */
    private void cancelItem(OrderDetail detail) {
        if (detail.getId() == null)
            return;

        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Nhập lý do huỷ món...");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 0);
        layout.addView(input);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy món")
                .setMessage("Hủy món " + (detail.getMenuItem() != null ? detail.getMenuItem().getItemName() : "") + "?")
                .setView(layout)
                .setPositiveButton("Hủy món", (dialog, which) -> {
                    String reason = input.getText().toString().trim();
                    ZappyApiService api = RetrofitClient.getApiService();

                    api.cancelItem(detail.getId(), reason, new java.util.HashMap<>()).enqueue(new Callback<Map>() {
                        @Override
                        public void onResponse(Call<Map> call, Response<Map> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ChiTietBanActivity.this, "Đã hủy món!", Toast.LENGTH_SHORT).show();
                                loadOrderDetails();
                            } else {
                                Toast.makeText(ChiTietBanActivity.this, "Lỗi hủy món", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map> call, Throwable t) {
                            Toast.makeText(ChiTietBanActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    /**
     * TẢI DANH SÁCH MÓN ĂN CỦA BÀN
     * Lấy dữ liệu từ Database thông qua API.
     */
    private void loadOrderDetails() {
        if (orderId == -1)
            return;
        ZappyApiService api = RetrofitClient.getApiService();
        api.getOrderDetails(orderId).enqueue(new Callback<List<OrderDetail>>() {
            @Override
            public void onResponse(Call<List<OrderDetail>> call, Response<List<OrderDetail>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<OrderDetail> details = response.body();
                    adapter.setItems(details);
                    updateSummary(details);
                    updateGuiButton(details);
                }
            }

            @Override
            public void onFailure(Call<List<OrderDetail>> call, Throwable t) {
            }
        });
    }

    /**
     * TÍNH TỔNG TIỀN HÓA ĐƠN
     * Bỏ qua các món đã bị hủy (Status = 2).
     */
    private void updateSummary(List<OrderDetail> details) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderDetail d : details) {
            if (d.getStatus() != null && d.getStatus() != 2 && d.getSubTotal() != null) {
                total = total.add(d.getSubTotal());
            }
        }
        tvTotalAmount.setText(formatter.format(total));
        tvBadgeCount.setText(details.size() + " MÓN");
    }

    /**
     * CẬP NHẬT NÚT GỬI BẾP
     * Sáng lên khi có món mới thêm (Status = 0) chưa được gửi.
     */
    private void updateGuiButton(List<OrderDetail> details) {
        if (btnGuiBep == null)
            return;
        boolean hasPending = false;
        for (OrderDetail d : details) {
            if (d.getStatus() != null && d.getStatus() == 0) {
                hasPending = true;
                break;
            }
        }
        btnGuiBep.setEnabled(hasPending);
        if (hasPending) {
            btnGuiBep.setBackgroundResource(R.drawable.bg_btn_primary);
        } else {
            btnGuiBep.setBackgroundResource(R.drawable.bg_btn_disabled);
        }
    }

    /**
     * TẢI LẠI DỮ LIỆU MỖI KHI MỞ LẠI MÀN HÌNH
     * Chống lỗi hiển thị sai trạng thái món khi vừa từ màn hình Thêm món quay về.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadOrderDetails();
    }

    /**
     * CÀI ĐẶT SỰ KIỆN CHO CÁC NÚT BẤM VÀ ĐIỀU HƯỚNG
     */
    private void setupClickListeners() {

        // Back -> Về trang trước đó (Danh sách order hoặc Sơ đồ bàn)
        btnBack.setOnClickListener(v -> {
            finish();
        });

        // Thêm món -> Lập order thêm cho bàn này
        btnThemMon.setOnClickListener(v -> {
            Intent intent = new Intent(this, LapOrderActivity.class);
            intent.putExtra("TABLE_ID", tableId);
            intent.putExtra("TABLE_NAME", tableName);
            startActivity(intent);
        });

        // Tính tiền -> Thanh toán
        btnTinhTien.setOnClickListener(v -> {
            Intent intent = new Intent(this, HoaDonActivity.class);
            intent.putExtra("ORDER_ID", orderId);
            intent.putExtra("TABLE_NAME", tableName);
            startActivity(intent);
        });

        if (btnGuiBep != null) {
            btnGuiBep.setOnClickListener(v -> guiBep());
        }
    }

    /**
     * HÀM BẤM NÚT GỬI BẾP TRỰC TIẾP TỪ MÀN HÌNH NÀY
     * Dùng khi có các món vừa được thêm nhanh nhưng chưa gửi.
     */
    private void guiBep() {
        if (orderId == -1)
            return;
        ZappyApiService api = RetrofitClient.getApiService();
        api.sendOrder(orderId).enqueue(new Callback<Map>() {
            @Override
            public void onResponse(Call<Map> call, Response<Map> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ChiTietBanActivity.this, "Đã gửi bếp thành công!", Toast.LENGTH_SHORT).show();
                    loadOrderDetails();
                } else {
                    Toast.makeText(ChiTietBanActivity.this, "Lỗi gửi bếp", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map> call, Throwable t) {
                Toast.makeText(ChiTietBanActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
