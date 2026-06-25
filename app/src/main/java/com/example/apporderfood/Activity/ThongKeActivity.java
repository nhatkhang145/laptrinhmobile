package com.example.apporderfood.Activity;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.apporderfood.R;
import com.mikepenz.iconics.view.IconicsImageView;

public class ThongKeActivity extends AppCompatActivity {

    // Views tiêu đề và điều hướng
    private IconicsImageView btnBack;
    private TextView tvTotalRevenue, tvGrowth, tvTotalOrders, tvCancelledItems, tvUnpaidTables;
    
    // Tabs bộ lọc
    private TextView tabToday, tabWeek, tabMonth;
    
    // Bottom Navigation Custom
    private View navOrder, navSoDo, navTienIch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_ke);

        initViews();
        setupListeners();
        
        // Mặc định load dữ liệu "Hôm nay"
        loadDataForTab("Hôm nay");
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        
        // Các TextView hiển thị dữ liệu động
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvGrowth = findViewById(R.id.tvGrowth);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvCancelledItems = findViewById(R.id.tvCancelledItems);
        tvUnpaidTables = findViewById(R.id.tvUnpaidTables);

        // Bộ lọc thời gian (Tabs)
        tabToday = findViewById(R.id.tabToday);
        tabWeek = findViewById(R.id.tabWeek);
        tabMonth = findViewById(R.id.tabMonth);

        // Bottom Navigation Custom (LinearLayouts)
        navOrder = findViewById(R.id.navOrder);
        navSoDo = findViewById(R.id.navSoDo);
        navTienIch = findViewById(R.id.navTienIch);
    }

    private void setupListeners() {
        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Xử lý chuyển đổi Tab "Hôm nay"
        tabToday.setOnClickListener(v -> {
            updateTabUI(tabToday);
            loadDataForTab("Hôm nay");
        });

        // Xử lý chuyển đổi Tab "Tuần này"
        tabWeek.setOnClickListener(v -> {
            updateTabUI(tabWeek);
            loadDataForTab("Tuần này");
        });

        // Xử lý chuyển đổi Tab "Tháng này"
        tabMonth.setOnClickListener(v -> {
            updateTabUI(tabMonth);
            loadDataForTab("Tháng này");
        });

        // Bottom Navigation Listeners
        navOrder.setOnClickListener(v -> {
            startActivity(new Intent(this, DanhSachOrderActivity.class));
            overridePendingTransition(0, 0);
        });
        navSoDo.setOnClickListener(v -> {
            startActivity(new Intent(this, SoDobanActivity.class));
            overridePendingTransition(0, 0);
        });
        navTienIch.setOnClickListener(v -> {
            startActivity(new Intent(this, TienIchActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
    }

    /**
     * Cập nhật giao diện (Màu sắc, Font chữ) khi người dùng chuyển Tab
     */
    private void updateTabUI(TextView selectedTab) {
        // Reset tất cả tab về trạng thái không được chọn
        TextView[] tabs = {tabToday, tabWeek, tabMonth};
        for (TextView tab : tabs) {
            tab.setBackground(null);
            tab.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            tab.setTypeface(null, Typeface.NORMAL);
        }

        // Làm nổi bật tab được chọn
        selectedTab.setBackgroundResource(R.drawable.bg_btn_primary);
        selectedTab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
        selectedTab.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        selectedTab.setTypeface(null, Typeface.BOLD);
    }

    /**
     * Giả lập việc tải dữ liệu từ Database/Firebase dựa trên thời gian
     */
    private void loadDataForTab(String timeRange) {
        // Sử dụng string format đã thiết lập trong strings.xml để đồng bộ
        if (timeRange.equals("Hôm nay")) {
            tvTotalRevenue.setText(String.format(getString(R.string.format_tien_te), "15.250.000"));
            tvTotalOrders.setText(String.format(getString(R.string.format_don_hang), 45));
            tvCancelledItems.setText(String.format(getString(R.string.format_mon), 3));
            tvUnpaidTables.setText(String.format(getString(R.string.format_ban), 5));
            tvGrowth.setText("+12.5% so với hôm qua");
        } else if (timeRange.equals("Tuần này")) {
            tvTotalRevenue.setText(String.format(getString(R.string.format_tien_te), "102.800.000"));
            tvTotalOrders.setText(String.format(getString(R.string.format_don_hang), 312));
            tvCancelledItems.setText(String.format(getString(R.string.format_mon), 12));
            tvUnpaidTables.setText(String.format(getString(R.string.format_ban), 2));
            tvGrowth.setText("+5.2% so với kỳ trước");
        } else {
            tvTotalRevenue.setText(String.format(getString(R.string.format_tien_te), "450.320.000"));
            tvTotalOrders.setText(String.format(getString(R.string.format_don_hang), 1250));
            tvCancelledItems.setText(String.format(getString(R.string.format_mon), 24));
            tvUnpaidTables.setText(String.format(getString(R.string.format_ban), 0));
            tvGrowth.setText("+15.8% so với tháng trước");
        }
    }
}
