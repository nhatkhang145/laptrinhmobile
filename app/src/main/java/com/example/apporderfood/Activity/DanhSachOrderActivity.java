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

public class DanhSachOrderActivity extends AppCompatActivity {

    private LinearLayout navOrder, navSoDo, navTienIch;
    private TextView tabTatCa, tabDangPhucVu, tvEmpty;
    private RecyclerView rvOrderList;
    private ProgressBar progressBar;
    private EditText etSearch;
    
    private OrderListAdapter adapter;
    private int resId = -1;
    private int currentUserId = -1;
    private int currentTab = 0; // 0 = Tất cả, 1 = Đang phục vụ
    private String currentSearchText = "";
    private List<java.util.Map<String, Object>> allOrders = new ArrayList<>();
    private ZappyApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_danh_sach_order);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); // Bottom is handled by ScrollView margin
            return insets;
        });

        apiService = RetrofitClient.getApiService();
        SharedPreferences prefs = getSharedPreferences("ZappySession", Context.MODE_PRIVATE);
        resId = prefs.getInt("RES_ID", -1);
        currentUserId = prefs.getInt("USER_ID", -1);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        
        loadTables();
    }

    private void initViews() {
        navOrder   = findViewById(R.id.navOrder);
        navSoDo    = findViewById(R.id.navSoDo);
        navTienIch = findViewById(R.id.navTienIch);

        tabTatCa = findViewById(R.id.tabTatCa);
        tabDangPhucVu = findViewById(R.id.tabDangPhucVu);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvOrderList = findViewById(R.id.rvOrderList);
        progressBar = findViewById(R.id.progressBar);
        etSearch = findViewById(R.id.etSearch);
    }
    
    private void setupRecyclerView() {
        adapter = new OrderListAdapter(this, order -> {
            java.util.Map<String, Object> table = (java.util.Map<String, Object>) order.get("table");
            if (table != null) {
                Intent intent = new Intent(this, ChiTietBanActivity.class);
                String areaName = "Không rõ";
                if (table.get("area") != null) {
                    java.util.Map<String, Object> area = (java.util.Map<String, Object>) table.get("area");
                    areaName = (String) area.get("areaName");
                }
                String title = areaName + " - " + table.get("tableName");
                intent.putExtra("TABLE_NAME", title);
                intent.putExtra("TABLE_ID", ((Number) table.get("id")).intValue());
                
                if (order.get("id") != null) {
                    intent.putExtra("ORDER_ID", ((Number) order.get("id")).intValue());
                }
                startActivity(intent);
            }
        });
        rvOrderList.setLayoutManager(new LinearLayoutManager(this));
        rvOrderList.setAdapter(adapter);
    }

    private void setupClickListeners() {
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

        tabTatCa.setOnClickListener(v -> {
            currentTab = 0;
            tabTatCa.setBackgroundResource(R.drawable.bg_tab_active_dark);
            tabTatCa.setTextColor(getResources().getColor(R.color.surface));
            
            tabDangPhucVu.setBackgroundResource(R.drawable.bg_tab_inactive);
            tabDangPhucVu.setTextColor(getResources().getColor(R.color.text_primary));
            
            filterList();
        });

        tabDangPhucVu.setOnClickListener(v -> {
            currentTab = 1;
            tabDangPhucVu.setBackgroundResource(R.drawable.bg_tab_active_dark);
            tabDangPhucVu.setTextColor(getResources().getColor(R.color.surface));
            
            tabTatCa.setBackgroundResource(R.drawable.bg_tab_inactive);
            tabTatCa.setTextColor(getResources().getColor(R.color.text_primary));
            
            
            filterList();
        });

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchText = s.toString().trim().toLowerCase();
                    filterList();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }
    
    private void loadTables() {
        if (resId == -1) return;
        
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvOrderList.setVisibility(View.GONE);
        
        apiService.getActiveOrdersByRestaurant(resId).enqueue(new Callback<List<java.util.Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<java.util.Map<String, Object>>> call, Response<List<java.util.Map<String, Object>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allOrders = response.body();
                    updateTabsCount();
                    filterList();
                } else {
                    Toast.makeText(DanhSachOrderActivity.this, "Lỗi lấy danh sách đơn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<java.util.Map<String, Object>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DanhSachOrderActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("DanhSachOrderActivity", "API Call failed", t);
            }
        });
    }
    
    private void updateTabsCount() {
        int countTatCa = 0;
        int countDangPhucVu = 0;
        
        for (java.util.Map<String, Object> order : allOrders) {
            countTatCa++; // Tất cả active orders
            if (order.get("user") != null) {
                java.util.Map<String, Object> user = (java.util.Map<String, Object>) order.get("user");
                if (user.get("id") != null && ((Number) user.get("id")).intValue() == currentUserId) {
                    countDangPhucVu++;
                }
            }
        }
        tabTatCa.setText("Tất cả (" + countTatCa + ")");
        tabDangPhucVu.setText("Đang phục vụ (" + countDangPhucVu + ")");
    }

    private void filterList() {
        List<java.util.Map<String, Object>> filtered = new ArrayList<>();
        for (java.util.Map<String, Object> order : allOrders) {
            boolean matchesTab = false;
            if (currentTab == 0) {
                matchesTab = true;
            } else if (currentTab == 1) {
                if (order.get("user") != null) {
                    java.util.Map<String, Object> user = (java.util.Map<String, Object>) order.get("user");
                    if (user.get("id") != null && ((Number) user.get("id")).intValue() == currentUserId) {
                        matchesTab = true;
                    }
                }
            }
            
            if (matchesTab) {
                if (currentSearchText.isEmpty()) {
                    filtered.add(order);
                } else {
                    java.util.Map<String, Object> table = (java.util.Map<String, Object>) order.get("table");
                    if (table != null && table.get("tableName") != null) {
                        String tblName = table.get("tableName").toString().toLowerCase();
                        if (tblName.contains(currentSearchText)) {
                            filtered.add(order);
                        }
                    }
                }
            }
        }
        
        adapter.setOrderList(filtered);
        if (filtered.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvOrderList.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvOrderList.setVisibility(View.VISIBLE);
        }
    }
}
