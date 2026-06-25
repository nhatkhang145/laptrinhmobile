package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apporderfood.R;
import com.example.apporderfood.adapter.MenuItemLapOrderAdapter;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.Category;
import com.example.apporderfood.model.MenuItem;
import com.google.android.material.tabs.TabLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * LapOrderActivity - Màn hình chọn món khi ấn vào bàn TRỐNG
 * Layout: activity_lap_order.xml
 * Flow:
 *  1. Nhận TABLE_ID, TABLE_NAME từ SoDobanActivity
 *  2. Gọi API POST /api/orders/open để tạo hóa đơn mới, bàn -> is_occupied = TRUE
 *  3. NV chọn món (thêm vào giỏ RAM tạm)
 *  4. Nút ĐỒNG Ý -> Gọi API thêm từng món -> Chuyển sang XacNhanOrderActivity
 *  5. Nút HỦY BỎ -> finish() về SoDobanActivity
 */
public class LapOrderActivity extends AppCompatActivity {

    private LinearLayout btnHuyBo;
    private LinearLayout btnDongY;
    private TabLayout tabLayoutCategories;
    private RecyclerView rvMenuItems;
    private EditText etSearch;

    private int tableId = -1;
    private String tableName = "";
    private int orderId = -1; // Sẽ được set sau khi gọi API openTable thành công
    private int resId = -1;

    private MenuItemLapOrderAdapter adapter;
    private List<Category> categoryList = new ArrayList<>();
    private ZappyApiService apiService;
    private Integer currentSelectedCatId = null; // null = Tất cả

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lap_order);

        // Nhận dữ liệu từ SoDobanActivity
        tableId   = getIntent().getIntExtra("TABLE_ID", -1);
        tableName = getIntent().getStringExtra("TABLE_NAME") != null
                ? getIntent().getStringExtra("TABLE_NAME") : "Bàn";

        android.content.SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        resId = prefs.getInt("RES_ID", -1);
        apiService = RetrofitClient.getApiService();

        initViews();
        setupRecyclerView();
        setupClickListeners();
        setupSearch();
        
        openTable(); // Bước 1: Tạo hóa đơn ngay khi mở màn hình
        loadCategories(); // Bước 2: Tải danh mục
    }

    private void initViews() {
        btnHuyBo   = findViewById(R.id.btnHuyBo);
        btnDongY   = findViewById(R.id.btnDongY);
        tabLayoutCategories = findViewById(R.id.tabLayoutCategories);
        rvMenuItems = findViewById(R.id.rvMenuItems);
        etSearch = findViewById(R.id.etSearch);
        
        // Cập nhật title Header
        LinearLayout headerLayout = findViewById(R.id.headerLayout);
        if (headerLayout != null && headerLayout.getChildCount() > 1) {
            View child = headerLayout.getChildAt(1);
            if (child instanceof TextView) {
                ((TextView) child).setText("Lập Order - " + tableName);
            }
        }
    }

    private void setupRecyclerView() {
        adapter = new MenuItemLapOrderAdapter(this, new ArrayList<>(), item -> {
            addItemToOrder(item);
        });
        rvMenuItems.setLayoutManager(new LinearLayoutManager(this));
        rvMenuItems.setAdapter(adapter);
    }

    /**
     * Bước 1: Gọi API POST /api/orders/open để tạo hóa đơn mới.
     * Backend sẽ: tạo dòng trong Orders + đặt Tables.is_occupied = TRUE
     */
    private void openTable() {
        if (tableId == -1) return;

        ZappyApiService api = RetrofitClient.getApiService();
        Map<String, Integer> data = new HashMap<>();
        data.put("tableId", tableId);
        
        android.content.SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        int userId = prefs.getInt("USER_ID", -1);
        if (userId != -1) {
            data.put("userId", userId);
        }

        api.openTable(data).enqueue(new Callback<Map>() {
            @Override
            public void onResponse(Call<Map> call, Response<Map> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Object id = response.body().get("orderId");
                    if (id != null) {
                        orderId = ((Double) id).intValue();
                    }
                } else {
                    Toast.makeText(LapOrderActivity.this,
                            "Không thể mở bàn, thử lại!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Map> call, Throwable t) {
                Toast.makeText(LapOrderActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadCategories() {
        if (resId == -1) return;
        
        apiService.getCategories(resId).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList = response.body();
                    setupTabs();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(LapOrderActivity.this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTabs() {
        tabLayoutCategories.removeAllTabs();
        
        // Tab "Tất cả"
        tabLayoutCategories.addTab(tabLayoutCategories.newTab().setText("Tất cả"));
        
        for (Category cat : categoryList) {
            tabLayoutCategories.addTab(tabLayoutCategories.newTab().setText(cat.getCatName()));
        }

        tabLayoutCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    currentSelectedCatId = null;
                } else if (position - 1 < categoryList.size()) {
                    currentSelectedCatId = categoryList.get(position - 1).getId();
                }
                loadMenuItems(etSearch.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Tải danh sách món cho "Tất cả" mặc định ban đầu
        loadMenuItems("");
    }

    private void loadMenuItems(String keyword) {
        if (resId == -1) return;

        Call<List<MenuItem>> call;
        if (currentSelectedCatId == null) {
            call = apiService.getMenuByRestaurant(resId, keyword);
        } else {
            if (keyword != null && !keyword.trim().isEmpty()) {
                call = apiService.getMenuByRestaurant(resId, keyword);
            } else {
                call = apiService.getMenuByCategory(currentSelectedCatId);
            }
        }

        call.enqueue(new Callback<List<MenuItem>>() {
            @Override
            public void onResponse(Call<List<MenuItem>> call, Response<List<MenuItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setMenuItems(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<MenuItem>> call, Throwable t) {
                Toast.makeText(LapOrderActivity.this, "Lỗi tải món ăn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addItemToOrder(MenuItem item) {
        if (orderId == -1) {
            Toast.makeText(this, "Chưa tạo được hóa đơn!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("menuItemId", item.getId());
        data.put("quantity", 1);
        data.put("price", item.getPrice());

        apiService.addItem(orderId, data).enqueue(new Callback<com.example.apporderfood.model.OrderDetail>() {
            @Override
            public void onResponse(Call<com.example.apporderfood.model.OrderDetail> call, Response<com.example.apporderfood.model.OrderDetail> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(LapOrderActivity.this, "Đã thêm " + item.getItemName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LapOrderActivity.this, "Lỗi khi thêm món", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.apporderfood.model.OrderDetail> call, Throwable t) {
                Toast.makeText(LapOrderActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadMenuItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {

        // Nút HỦY BỎ -> quay lại sơ đồ bàn
        btnHuyBo.setOnClickListener(v -> finish());

        // Nút ĐỒNG Ý -> Chuyển sang màn hình xác nhận / chi tiết order
        btnDongY.setOnClickListener(v -> {
            if (orderId == -1) {
                Toast.makeText(this, "Đang tạo đơn, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, XacNhanOrderActivity.class);
            intent.putExtra("ORDER_ID", orderId);
            intent.putExtra("TABLE_NAME", tableName);
            intent.putExtra("TABLE_ID", tableId);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
