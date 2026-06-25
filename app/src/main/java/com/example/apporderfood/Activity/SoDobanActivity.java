package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apporderfood.R;

/**
 * SoDobanActivity - Màn hình Sơ đồ bàn (tab giữa của bottom nav)
 * - Hiển thị lưới bàn ăn theo khu vực
 * - Click bàn TRỐNG  -> LapOrderActivity (lập order mới)
 * - Click bàn CÓ KHÁCH -> ThongTinBanActivity (xem thông tin / chi tiết)
 * - Bottom nav: Order -> DanhSachOrderActivity, Tiện ích -> TienIchActivity
 */
public class SoDobanActivity extends AppCompatActivity {

    private LinearLayout navOrder;
    private LinearLayout navSoDo;
    private LinearLayout navTienIch;

    // Tab khu vực
    private TextView tabKhuA;
    private TextView tabKhuB;
    private TextView tabKhuC;

    // Bàn mẫu (trạng thái sẽ được load từ API trong thực tế)
    private LinearLayout cardBan01;
    private LinearLayout cardBan02;
    private LinearLayout cardBan03;
    private LinearLayout cardBan04;
    private LinearLayout cardBan05;
    private LinearLayout cardBan06;
    private LinearLayout cardBan07;
    private LinearLayout cardBan08;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_so_do_ban);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        navOrder   = findViewById(R.id.navOrder);
        navSoDo    = findViewById(R.id.navSoDo);
        navTienIch = findViewById(R.id.navTienIch);

        tabKhuA = findViewById(R.id.tabKhuA);
        tabKhuB = findViewById(R.id.tabKhuB);
        tabKhuC = findViewById(R.id.tabKhuC);

        cardBan01 = findViewById(R.id.cardBan01);
        cardBan02 = findViewById(R.id.cardBan02);
        cardBan03 = findViewById(R.id.cardBan03);
        cardBan04 = findViewById(R.id.cardBan04);
        cardBan05 = findViewById(R.id.cardBan05);
        cardBan06 = findViewById(R.id.cardBan06);
        cardBan07 = findViewById(R.id.cardBan07);
        cardBan08 = findViewById(R.id.cardBan08);
    }

    private void setupClickListeners() {

        // ---- Bottom Navigation ----
        navOrder.setOnClickListener(v -> {
            startActivity(new Intent(this, DanhSachOrderActivity.class));
            overridePendingTransition(0, 0);
        });

        navSoDo.setOnClickListener(v -> { /* Đang ở trang này rồi */ });

        navTienIch.setOnClickListener(v -> {
            startActivity(new Intent(this, TienIchActivity.class));
            overridePendingTransition(0, 0);
        });

        // ---- Bàn TRỐNG -> Lập Order ----
        cardBan01.setOnClickListener(v -> openLapOrder("Bàn 01", 1));
        cardBan03.setOnClickListener(v -> openLapOrder("Bàn 03", 3));
        cardBan04.setOnClickListener(v -> openLapOrder("Bàn 04", 4));
        cardBan06.setOnClickListener(v -> openLapOrder("Bàn 06", 6));
        cardBan07.setOnClickListener(v -> openLapOrder("Bàn 07", 7));
        cardBan08.setOnClickListener(v -> openLapOrder("Bàn 08", 8));

        // ---- Bàn CÓ KHÁCH -> Thông tin bàn ----
        cardBan02.setOnClickListener(v -> openThongTinBan("Bàn 02", 2));
        cardBan05.setOnClickListener(v -> openThongTinBan("Bàn 05", 5));
    }

    /** Mở màn hình Lập Order cho bàn trống */
    private void openLapOrder(String tableName, int tableId) {
        Intent intent = new Intent(this, LapOrderActivity.class);
        intent.putExtra("TABLE_NAME", tableName);
        intent.putExtra("TABLE_ID", tableId);
        startActivity(intent);
    }

    /** Mở popup Thông tin bàn cho bàn đang có khách */
    private void openThongTinBan(String tableName, int tableId) {
        Intent intent = new Intent(this, ThongTinBanActivity.class);
        intent.putExtra("TABLE_NAME", tableName);
        intent.putExtra("TABLE_ID", tableId);
        startActivity(intent);
    }
}
