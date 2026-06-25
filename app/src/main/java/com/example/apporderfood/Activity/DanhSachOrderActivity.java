package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apporderfood.R;

/**
 * DanhSachOrderActivity - Tab ORDER - Danh sách các bàn đang phục vụ
 * Layout: activity_danh_sach_order.xml
 * Flow:
 *  - Hiển thị danh sách Orders (status=0 - đang phục vụ) của nhà hàng
 *  - Tabs: "Tất cả" / "Đang phục vụ"
 *  - Click "Chi tiết" trên từng card -> ChiTietBanActivity
 *  - Bottom nav: Sơ đồ -> SoDobanActivity, Tiện ích -> TienIchActivity
 */
public class DanhSachOrderActivity extends AppCompatActivity {

    private LinearLayout navOrder;
    private LinearLayout navSoDo;
    private LinearLayout navTienIch;

    private LinearLayout btnChiTietA01;
    private LinearLayout btnChiTietB02;
    private LinearLayout btnChiTietE01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_sach_order);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        navOrder   = findViewById(R.id.navOrder);
        navSoDo    = findViewById(R.id.navSoDo);
        navTienIch = findViewById(R.id.navTienIch);

        btnChiTietA01 = findViewById(R.id.btnChiTietA01);
        btnChiTietB02 = findViewById(R.id.btnChiTietB02);
        btnChiTietE01 = findViewById(R.id.btnChiTietE01);
    }

    private void setupClickListeners() {

        // ---- Bottom Navigation ----
        navOrder.setOnClickListener(v -> { /* Đang ở trang này */ });

        navSoDo.setOnClickListener(v -> {
            startActivity(new Intent(this, SoDobanActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        navTienIch.setOnClickListener(v -> {
            startActivity(new Intent(this, TienIchActivity.class));
            overridePendingTransition(0, 0);
        });

        // ---- Chi tiết từng bàn -> ChiTietBanActivity ----
        btnChiTietA01.setOnClickListener(v -> openChiTiet("Bàn A01", 1, 1));
        btnChiTietB02.setOnClickListener(v -> openChiTiet("BÀN B02", 2, 2));
        btnChiTietE01.setOnClickListener(v -> openChiTiet("Bàn E01", 3, 3));
    }

    private void openChiTiet(String tableName, int tableId, int orderId) {
        Intent intent = new Intent(this, ChiTietBanActivity.class);
        intent.putExtra("TABLE_NAME", tableName);
        intent.putExtra("TABLE_ID", tableId);
        intent.putExtra("ORDER_ID", orderId);
        startActivity(intent);
    }
}
