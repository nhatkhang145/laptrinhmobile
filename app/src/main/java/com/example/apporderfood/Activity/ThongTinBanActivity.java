package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * ThongTinBanActivity - Popup hiển thị khi click vào bàn đang CÓ KHÁCH
 * Layout: activity_thong_tin_ban.xml
 * - Hiển thị: Tên bàn, tổng tiền, thời gian đã ngồi
 * - Nút "XEM CHI TIẾT ĐƠN" -> ChiTietBanActivity
 * - Nút "ĐÓNG"              -> Quay lại SoDobanActivity
 * - Nhấn ngoài dim overlay  -> Quay lại
 */
public class ThongTinBanActivity extends AppCompatActivity {

    private LinearLayout navOrder;
    private LinearLayout navSoDo;
    private LinearLayout navTienIch;
    private FrameLayout dimOverlay;
    private LinearLayout btnViewDetail;
    private TextView btnClose;
    private TextView tvPopupTableName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.apporderfood.R.layout.activity_thong_tin_ban);

        initViews();
        loadTableData();
        setupClickListeners();
    }

    private void initViews() {
        navOrder      = findViewById(com.example.apporderfood.R.id.navOrder);
        navSoDo       = findViewById(com.example.apporderfood.R.id.navSoDo);
        navTienIch    = findViewById(com.example.apporderfood.R.id.navTienIch);
        dimOverlay    = findViewById(com.example.apporderfood.R.id.dimOverlay);
        btnViewDetail = findViewById(com.example.apporderfood.R.id.btnViewDetail);
        btnClose      = findViewById(com.example.apporderfood.R.id.btnClose);
        tvPopupTableName = findViewById(com.example.apporderfood.R.id.tvPopupTableName);
    }

    /** Đọc dữ liệu được truyền từ SoDobanActivity */
    private void loadTableData() {
        String tableName = getIntent().getStringExtra("TABLE_NAME");
        if (tableName != null && tvPopupTableName != null) {
            tvPopupTableName.setText(tableName);
        }
    }

    private void setupClickListeners() {

        // Nhấn ngoài card (dim overlay) -> đóng popup
        dimOverlay.setOnClickListener(v -> finish());

        // Nút ĐÓNG
        btnClose.setOnClickListener(v -> finish());

        // Nút XEM CHI TIẾT ĐƠN -> ChiTietBanActivity
        btnViewDetail.setOnClickListener(v -> {
            int tableId = getIntent().getIntExtra("TABLE_ID", -1);
            Intent intent = new Intent(this, ChiTietBanActivity.class);
            intent.putExtra("TABLE_ID", tableId);
            intent.putExtra("TABLE_NAME", getIntent().getStringExtra("TABLE_NAME"));
            startActivity(intent);
            finish();
        });

        // ---- Bottom Navigation ----
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
