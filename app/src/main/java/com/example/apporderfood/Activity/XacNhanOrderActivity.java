package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.R;
import com.example.apporderfood.adapter.OrderDetailAdapter;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.OrderDetail;
import com.example.apporderfood.model.CartItem;
import com.example.apporderfood.model.MenuItem;
import com.example.apporderfood.Activity.LapOrderActivity;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * XacNhanOrderActivity - Màn hình xem danh sách món đã gọi & Gửi đơn bếp
 * Layout: activity_xac_nhan_order.xml
 * Flow:
 * - Nhận ORDER_ID từ LapOrderActivity
 * - Gọi API GET /api/orders/{orderId}/details để tải danh sách món
 * - Hiển thị trong RecyclerView với tổng tiền ở bottom bar
 * - Nút "Thêm món" -> quay lại LapOrderActivity
 * - Nút "GỬI BẾP" -> Gọi API PUT /api/orders/{id}/send -> sang
 * ChiTietBanActivity
 * - Nút Back -> về SoDobanActivity
 */
public class XacNhanOrderActivity extends AppCompatActivity {

    private View btnBack;
    private LinearLayout btnGui;
    private LinearLayout btnThemMon;
    private View btnTinhTien;
    private RecyclerView rvOrderDetails;
    private ProgressBar progressBar;
    private TextView tvTitle;
    private TextView tvBadgeCount;
    private TextView tvItemCount;
    private TextView tvTotalAmount;

    private int orderId = -1;
    private int tableId = -1;
    private String tableName = "";

    private OrderDetailAdapter adapter;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_xac_nhan_order);
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
        loadOrderDetails(); // Gọi API lấy danh sách món
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnGui = findViewById(R.id.btnGui);
        btnThemMon = findViewById(R.id.btnThemMon);
        btnTinhTien = findViewById(R.id.btnTinhTien);
        rvOrderDetails = findViewById(R.id.rvOrderDetails);
        progressBar = findViewById(R.id.progressBar);
        tvTitle = findViewById(R.id.tvTitle);
        tvBadgeCount = findViewById(R.id.tvBadgeCount);
        tvItemCount = findViewById(R.id.tvItemCount);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);

        if (tvTitle != null) {
            tvTitle.setText("Xác nhận - " + tableName);
        }
    }

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

    private void cancelItem(OrderDetail detail) {
        if (detail.getId() == null) {
            if (detail.getMenuItem() != null && LapOrderActivity.cartMap.containsKey(detail.getMenuItem().getId())) {
                LapOrderActivity.cartMap.remove(detail.getMenuItem().getId());
                Toast.makeText(XacNhanOrderActivity.this, "Đã xóa món khỏi giỏ!", Toast.LENGTH_SHORT).show();
                loadOrderDetails();
            }
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy món")
                .setMessage("Hủy món " + (detail.getMenuItem() != null ? detail.getMenuItem().getItemName() : "") + "?")
                .setPositiveButton("Hủy món", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    ZappyApiService api = RetrofitClient.getApiService();
                    Map<String, Integer> data = new java.util.HashMap<>();

                    api.cancelItem(detail.getId(), data).enqueue(new Callback<Map>() {
                        @Override
                        public void onResponse(Call<Map> call, Response<Map> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(XacNhanOrderActivity.this, "Đã hủy món!", Toast.LENGTH_SHORT).show();
                                loadOrderDetails();
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(XacNhanOrderActivity.this, "Lỗi hủy món", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(XacNhanOrderActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void loadOrderDetails() {
        if (orderId == -1) {
            Toast.makeText(this, "Không tìm thấy đơn hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        rvOrderDetails.setVisibility(View.GONE);

        ZappyApiService api = RetrofitClient.getApiService();
        api.getOrderDetails(orderId).enqueue(new Callback<List<OrderDetail>>() {
            @Override
            public void onResponse(Call<List<OrderDetail>> call, Response<List<OrderDetail>> response) {
                progressBar.setVisibility(View.GONE);
                rvOrderDetails.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    List<OrderDetail> details = response.body();
                    
                    for (CartItem ci : LapOrderActivity.cartMap.values()) {
                        OrderDetail localItem = new OrderDetail();
                        localItem.setId(null);
                        localItem.setMenuItem(ci.getMenuItem());
                        localItem.setQuantity(ci.getQuantity());
                        localItem.setNote(ci.getNote());
                        BigDecimal price = ci.getMenuItem().getPrice();
                        localItem.setPriceAtSale(price); 
                        localItem.setStatus(0);
                        
                        details.add(0, localItem);
                    }

                    adapter.setItems(details);
                    updateSummary(details);
                    updateGuiButton(details);
                } else {
                    Toast.makeText(XacNhanOrderActivity.this,
                            "Không tải được danh sách món!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<OrderDetail>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(XacNhanOrderActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSummary(List<OrderDetail> details) {
        BigDecimal total = BigDecimal.ZERO;
        int totalQty = 0;

        for (OrderDetail d : details) {
            if (d.getSubTotal() != null) {
                total = total.add(d.getSubTotal());
            }
            if (d.getQuantity() != null) {
                totalQty += d.getQuantity();
            }
        }

        tvTotalAmount.setText(formatter.format(total));
        tvItemCount.setText(totalQty + " món");
        tvBadgeCount.setText(details.size() + " MÓN");
    }

    private void updateGuiButton(List<OrderDetail> details) {
        boolean hasPending = false;
        for (OrderDetail d : details) {
            if (d.getStatus() != null && d.getStatus() == 0) {
                hasPending = true;
                break;
            }
        }
        btnGui.setEnabled(hasPending);
        if (hasPending) {
            btnGui.setBackgroundResource(R.drawable.bg_btn_primary);
            btnGui.setAlpha(1.0f);
        } else {
            btnGui.setBackgroundResource(R.drawable.bg_btn_disabled);
            btnGui.setAlpha(1.0f);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrderDetails();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    private void setupClickListeners() {

        btnBack.setOnClickListener(v -> finish());

        btnThemMon.setOnClickListener(v -> {
            Intent intent = new Intent(this, LapOrderActivity.class);
            intent.putExtra("TABLE_ID", tableId);
            intent.putExtra("TABLE_NAME", tableName);
            startActivity(intent);
        });

        btnGui.setOnClickListener(v -> sendOrder());

        if (btnTinhTien != null) {
            btnTinhTien.setOnClickListener(v -> {
                Intent intent = new Intent(this, HoaDonActivity.class);
                intent.putExtra("ORDER_ID", orderId);
                intent.putExtra("TABLE_NAME", tableName);
                startActivity(intent);
            });
        }
    }

    private void sendOrder() {
        if (orderId == -1) return;
        btnGui.setEnabled(false);
        btnGui.setAlpha(0.45f);
        ZappyApiService api = RetrofitClient.getApiService();

        if (!LapOrderActivity.cartMap.isEmpty()) {
            List<Map<String, Object>> batchData = new ArrayList<>();
            for (CartItem ci : LapOrderActivity.cartMap.values()) {
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("itemId", ci.getMenuItem().getId());
                item.put("quantity", ci.getQuantity());
                item.put("note", ci.getNote() != null ? ci.getNote() : "");
                batchData.add(item);
            }
            api.addBatchItems(orderId, batchData).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        executeSendOrderAPI(api);
                    } else {
                        btnGui.setEnabled(true);
                        btnGui.setAlpha(1.0f);
                        Toast.makeText(XacNhanOrderActivity.this, "Lỗi thêm món trước khi gửi!", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    btnGui.setEnabled(true);
                    btnGui.setAlpha(1.0f);
                    Toast.makeText(XacNhanOrderActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            executeSendOrderAPI(api);
        }
    }

    private void executeSendOrderAPI(ZappyApiService api) {
        api.sendOrder(orderId).enqueue(new Callback<java.util.Map>() {
            @Override
            public void onResponse(Call<java.util.Map> call, Response<java.util.Map> response) {
                if (response.isSuccessful()) {
                        Toast.makeText(XacNhanOrderActivity.this,
                                "Đã gửi lên bếp thành công!", Toast.LENGTH_SHORT).show();
                        LapOrderActivity.cartMap.clear();
                        if (LapOrderActivity.instance != null) {
                            LapOrderActivity.instance.finish();
                        }

                        Intent intent = new Intent(XacNhanOrderActivity.this, ChiTietBanActivity.class);
                        intent.putExtra("ORDER_ID", orderId);
                        intent.putExtra("TABLE_ID", tableId);
                        intent.putExtra("TABLE_NAME", tableName);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                } else {
                    btnGui.setEnabled(true);
                    btnGui.setAlpha(1.0f);
                    Toast.makeText(XacNhanOrderActivity.this,
                            "Gửi đơn thất bại, thử lại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<java.util.Map> call, Throwable t) {
                btnGui.setEnabled(true);
                btnGui.setAlpha(1.0f);
                Toast.makeText(XacNhanOrderActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
