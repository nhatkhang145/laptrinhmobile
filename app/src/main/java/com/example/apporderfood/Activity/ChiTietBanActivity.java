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

/**
 * ChiTietBanActivity - Chi tiết đơn đang phục vụ tại bàn
 * Layout: activity_chi_tiet_ban.xml
 * Flow:
 *  - Hiển thị danh sách Order_Details theo orderId
 *  - Nút "Thêm món" -> LapOrderActivity (NV thêm thêm món mới, status=0)
 *  - Nút "Tính tiền" -> HoaDonActivity (Thanh toán, đóng hóa đơn)
 *  - Nút Hủy món (chỉ Quản lý) -> Gọi API cancelItem (status -> 2)
 *  - Nút Back -> DanhSachOrderActivity
 */
public class ChiTietBanActivity extends AppCompatActivity {

    private View btnBack;
    private View btnThemMon;
    private View btnTinhTien;
    
    private TextView tvTitle;
    private TextView tvBadgeCount;
    private TextView tvTotalAmount;
    private RecyclerView rvOrderDetails;
    private OrderDetailAdapter adapter;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    private int orderId   = -1;
    private int tableId   = -1;
    private String tableName = "";

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

        orderId   = getIntent().getIntExtra("ORDER_ID", -1);
        tableId   = getIntent().getIntExtra("TABLE_ID", -1);
        tableName = getIntent().getStringExtra("TABLE_NAME") != null
                ? getIntent().getStringExtra("TABLE_NAME") : "Bàn";

        initViews();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initViews() {
        btnBack     = findViewById(R.id.btnBack);
        btnThemMon  = findViewById(R.id.btnAddItem); // changed to btnAddItem based on xml id
        btnTinhTien = findViewById(R.id.btnTinhTien);
        
        tvTitle = findViewById(R.id.tvTitle);
        tvBadgeCount = findViewById(R.id.tvBadgeCount);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        rvOrderDetails = findViewById(R.id.rvOrderDetails);

        if (tvTitle != null) {
            tvTitle.setText(tableName + " • Đang phục vụ");
        }

        // We use btnAddItem from the XML instead of btnThemMon since XML has id btnAddItem
        if (btnThemMon == null) {
            btnThemMon = findViewById(R.id.btnThemMon);
        }
    }

    private void setupRecyclerView() {
        android.content.SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        boolean isAdmin = prefs.getInt("ROLE", 0) == 1;

        adapter = new OrderDetailAdapter(this, new java.util.ArrayList<>(), isAdmin, this::cancelItem);
        rvOrderDetails.setLayoutManager(new LinearLayoutManager(this));
        rvOrderDetails.setAdapter(adapter);
        rvOrderDetails.setNestedScrollingEnabled(false);
    }

    private void cancelItem(OrderDetail detail) {
        if (detail.getId() == null) return;
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận hủy món")
            .setMessage("Hủy món " + (detail.getMenuItem() != null ? detail.getMenuItem().getItemName() : "") + "?")
            .setPositiveButton("Hủy món", (dialog, which) -> {
                ZappyApiService api = RetrofitClient.getApiService();
                Map<String, Integer> data = new java.util.HashMap<>();
                api.cancelItem(detail.getId(), data).enqueue(new Callback<Map>() {
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

    private void loadOrderDetails() {
        if (orderId == -1) return;
        ZappyApiService api = RetrofitClient.getApiService();
        api.getOrderDetails(orderId).enqueue(new Callback<List<OrderDetail>>() {
            @Override
            public void onResponse(Call<List<OrderDetail>> call, Response<List<OrderDetail>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<OrderDetail> details = response.body();
                    adapter.setItems(details);
                    updateSummary(details);
                }
            }
            @Override
            public void onFailure(Call<List<OrderDetail>> call, Throwable t) {}
        });
    }

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

    @Override
    protected void onResume() {
        super.onResume();
        loadOrderDetails();
    }

    private void setupClickListeners() {

        // Back -> Về danh sách order
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, DanhSachOrderActivity.class));
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
    }
}
