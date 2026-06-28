package com.example.apporderfood.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.apporderfood.R;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.mikepenz.iconics.view.IconicsImageView;

import java.text.DecimalFormat;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThongKeActivity extends AppCompatActivity {

    private IconicsImageView btnBack;
    private TextView tvTotalRevenue, tvTotalOrders, tvCancelledItems, tvUnpaidTables;
    private TextView tabToday, tabWeek, tabMonth;
    private View navOrder, navSoDo, navTienIch;

    private ZappyApiService apiService;
    private int currentResId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_thong_ke);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        apiService = RetrofitClient.getApiService();
        SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        currentResId = prefs.getInt("RES_ID", -1);

        initViews();
        setupListeners();




        // Mặc định load dữ liệu "Hôm nay"
        selectTab(tabToday, "today");
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);

        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvCancelledItems = findViewById(R.id.tvCancelledItems);
        tvUnpaidTables = findViewById(R.id.tvUnpaidTables);

        tabToday = findViewById(R.id.tabToday);
        tabWeek = findViewById(R.id.tabWeek);
        tabMonth = findViewById(R.id.tabMonth);

        navOrder = findViewById(R.id.navOrder);
        navSoDo = findViewById(R.id.navSoDo);
        navTienIch = findViewById(R.id.navTienIch);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Call API theo period (Hôm nay, Tuần, Tháng)
        tabToday.setOnClickListener(v -> selectTab(tabToday, "today"));
        tabWeek.setOnClickListener(v -> selectTab(tabWeek, "week"));
        tabMonth.setOnClickListener(v -> selectTab(tabMonth, "month"));

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

    private void selectTab(TextView selectedTab, String period) {
        // Reset Style
        TextView[] tabs = {tabToday, tabWeek, tabMonth};
        for (TextView tab : tabs) {
            tab.setBackground(null);
            tab.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            tab.setTypeface(null, Typeface.NORMAL);
        }

        // Active Style
        selectedTab.setBackgroundResource(R.drawable.bg_btn_primary);
        selectedTab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
        selectedTab.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        selectedTab.setTypeface(null, Typeface.BOLD);

        // Fetch Data từ Server
        fetchStats(period);
    }

    private void fetchStats(String period) {
        if (currentResId == -1) return;

        tvTotalRevenue.setText("Đang tính...");

        apiService.getDashboardStats(currentResId, period).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Map<String, Object> data = response.body();


                        double revenue = ((Number) data.get("totalRevenue")).doubleValue();
                        int orders = ((Number) data.get("totalOrders")).intValue();
                        int cancelled = ((Number) data.get("cancelledItems")).intValue();
                        int unpaid = ((Number) data.get("unpaidTables")).intValue();

                        DecimalFormat formatter = new DecimalFormat("#,###");

                        tvTotalRevenue.setText(formatter.format(revenue) + " đ");
                        tvTotalOrders.setText(orders + " đơn");
                        tvCancelledItems.setText(cancelled + " món");
                        tvUnpaidTables.setText(unpaid + " bàn");

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ThongKeActivity.this, "Lỗi đọc dữ liệu thống kê!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    tvTotalRevenue.setText("Lỗi " + response.code());
                    Toast.makeText(ThongKeActivity.this, "Lỗi từ Server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ThongKeActivity.this, "Mất kết nối mạng!", Toast.LENGTH_SHORT).show();
                tvTotalRevenue.setText("Lỗi mạng");
            }
        });
    }
}