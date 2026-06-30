package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.apporderfood.R;

import androidx.appcompat.app.AppCompatActivity;

public class ThongTinBanActivity extends AppCompatActivity {

    private LinearLayout navOrder;
    private LinearLayout navSoDo;
    private LinearLayout navTienIch;

    private FrameLayout dimOverlay;

    private LinearLayout btnViewDetail;
    private TextView btnClose;

    private TextView tvPopupTableName;
    private TextView tvPopupAmount;
    private TextView tvPopupTime;

    private int currentOrderId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_tin_ban);

        initViews();
        loadTableData();
        setupClickListeners();
    }

    // Ánh xạ các view
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

    // Tải dữ liệu tên bàn và gọi API lấy thông tin hóa đơn
    private void loadTableData() {
        String tableName = getIntent().getStringExtra("TABLE_NAME");
        if (tableName != null && tvPopupTableName != null) {
            tvPopupTableName.setText(tableName);
        }

        tvPopupAmount.setText("...");
        tvPopupTime.setText("Đang tải...");

        int tableId = getIntent().getIntExtra("TABLE_ID", -1);
        if (tableId != -1) {
            fetchActiveOrder(tableId);
        }
    }

    // Gọi API lấy hóa đơn active và tính tổng tiền
    private void fetchActiveOrder(int tableId) {
        com.example.apporderfood.api.RetrofitClient.getApiService().getActiveOrder(tableId)
            .enqueue(new retrofit2.Callback<java.util.Map>() {

                @Override
                public void onResponse(retrofit2.Call<java.util.Map> call,
                                       retrofit2.Response<java.util.Map> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        java.util.Map body = response.body();

                        // Lưu id hóa đơn để dùng khi bấm chi tiết
                        Object orderIdObj = body.get("id");
                        if (orderIdObj != null) {
                            int orderId = ((Number) orderIdObj).intValue();
                            currentOrderId = orderId;

                            // Gọi API lấy chi tiết món của hóa đơn
                            com.example.apporderfood.api.RetrofitClient.getApiService()
                                .getOrderDetails(orderId)
                                .enqueue(new retrofit2.Callback<java.util.List<com.example.apporderfood.model.OrderDetail>>() {

                                    @Override
                                    public void onResponse(
                                            retrofit2.Call<java.util.List<com.example.apporderfood.model.OrderDetail>> call,
                                            retrofit2.Response<java.util.List<com.example.apporderfood.model.OrderDetail>> detailResponse) {

                                        if (detailResponse.isSuccessful() && detailResponse.body() != null) {
                                            double total = 0;
                                            // Cộng tiền các món không bị hủy
                                            for (com.example.apporderfood.model.OrderDetail d : detailResponse.body()) {
                                                if (d.getStatus() != 2) {
                                                    total += d.getQuantity() * d.getPriceAtSale().doubleValue();
                                                }
                                            }
                                            java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");
                                            tvPopupAmount.setText(formatter.format(total) + "đ");
                                        } else {
                                            tvPopupAmount.setText("0đ");
                                        }
                                    }

                                    @Override
                                    public void onFailure(
                                            retrofit2.Call<java.util.List<com.example.apporderfood.model.OrderDetail>> call,
                                            Throwable t) {
                                        tvPopupAmount.setText("Lỗi");
                                    }
                                });
                        } else {
                            tvPopupAmount.setText("0đ");
                        }

                        // Tính số thời gian đã phục vụ
                        if (body.get("createdAt") != null) {
                            String createdAtStr = (String) body.get("createdAt");
                            try {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    java.time.LocalDateTime createdAt = java.time.LocalDateTime.parse(createdAtStr);

                                    long minutes = java.time.temporal.ChronoUnit.MINUTES
                                            .between(createdAt, java.time.LocalDateTime.now());

                                    if (minutes < 60) {
                                        tvPopupTime.setText(minutes + " phút đã trôi qua");
                                    } else {
                                        long hours = minutes / 60;
                                        long mins  = minutes % 60;
                                        tvPopupTime.setText(hours + " giờ " + mins + " phút đã trôi qua");
                                    }
                                } else {
                                    tvPopupTime.setText("Đang phục vụ");
                                }
                            } catch (Exception e) {
                                tvPopupTime.setText("Đang phục vụ");
                            }
                        } else {
                            tvPopupTime.setText("Vừa mới mở");
                        }
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<java.util.Map> call, Throwable t) {
                    tvPopupAmount.setText("Lỗi");
                    tvPopupTime.setText("...");
                }
            });
    }

    // Lắng nghe sự kiện click các nút
    private void setupClickListeners() {

        dimOverlay.setOnClickListener(v -> finish());

        btnClose.setOnClickListener(v -> finish());

        // Bấm xem chi tiết thì chuyển sang màn hình chi tiết
        btnViewDetail.setOnClickListener(v -> {
            if (currentOrderId != -1) {
                int tableId = getIntent().getIntExtra("TABLE_ID", -1);
                Intent intent = new Intent(this, ChiTietBanActivity.class);
                intent.putExtra("TABLE_ID", tableId);
                intent.putExtra("TABLE_NAME", getIntent().getStringExtra("TABLE_NAME"));
                intent.putExtra("ORDER_ID", currentOrderId);
                startActivity(intent);
                finish();
            } else {
                android.widget.Toast.makeText(this, "Đang tải dữ liệu...",
                        android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        navOrder.setOnClickListener(v -> {
            startActivity(new Intent(this, DanhSachOrderActivity.class));
            overridePendingTransition(0, 0);
        });

        navSoDo.setOnClickListener(v -> {
            startActivity(new Intent(this, SoDobanActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        navTienIch.setOnClickListener(v -> {
            startActivity(new Intent(this, TienIchActivity.class));
            overridePendingTransition(0, 0);
        });
    }
}
