package com.example.apporderfood.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.R;
import com.example.apporderfood.adapter.TableAdapter;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.Area;
import com.example.apporderfood.model.TableModel;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SoDobanActivity extends AppCompatActivity {

    private LinearLayout navOrder;
    private LinearLayout navSoDo;
    private LinearLayout navTienIch;

    private TextView tvSelectedArea;
    private TabLayout tabLayoutAreas;
    private RecyclerView rvTables;
    private TableAdapter tableAdapter;

    private TextView tvEmptyCount;
    private TextView tvOccupiedCount;

    private ZappyApiService apiService;
    private List<Area> areaList = new ArrayList<>();
    private List<TableModel> tableList = new ArrayList<>();
    private int restaurantId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_so_do_ban);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        apiService = RetrofitClient.getApiService();

        SharedPreferences prefs = getSharedPreferences("ZappySession", Context.MODE_PRIVATE);
        restaurantId = prefs.getInt("RES_ID", -1);

        initViews();
        setupClickListeners();
        setupRecyclerView();

        if (restaurantId != -1) {
            loadAreas();
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID nhà hàng!", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        navOrder = findViewById(R.id.navOrder);
        navSoDo = findViewById(R.id.navSoDo);
        navTienIch = findViewById(R.id.navTienIch);

        tvSelectedArea = findViewById(R.id.tvSelectedArea);
        tabLayoutAreas = findViewById(R.id.tabLayoutAreas);
        rvTables = findViewById(R.id.rvTables);
        tvEmptyCount = findViewById(R.id.tvEmptyCount);
        tvOccupiedCount = findViewById(R.id.tvOccupiedCount);
    }

    private void setupRecyclerView() {
        tableAdapter = new TableAdapter(this, tableList, table -> {
            if (table.isOccupied()) {
                openThongTinBan(table.getTableName(), table.getId());
            } else {
                openLapOrder(table.getTableName(), table.getId());
            }
        });
        rvTables.setAdapter(tableAdapter);
    }

    private void loadAreas() {
        apiService.getAreas(restaurantId).enqueue(new Callback<List<Area>>() {
            @Override
            public void onResponse(Call<List<Area>> call, Response<List<Area>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    areaList = response.body();
                    setupTabs();
                } else {
                    Toast.makeText(SoDobanActivity.this, "Lỗi khi tải danh sách khu vực", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Area>> call, Throwable t) {
                Toast.makeText(SoDobanActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTabs() {
        tabLayoutAreas.removeAllTabs();
        for (Area area : areaList) {
            tabLayoutAreas.addTab(tabLayoutAreas.newTab().setText(area.getAreaName()));
        }

        tabLayoutAreas.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position >= 0 && position < areaList.size()) {
                    Area selectedArea = areaList.get(position);
                    tvSelectedArea.setText(selectedArea.getAreaName());
                    loadTablesByArea(selectedArea.getId());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // Load tables for first tab
        if (!areaList.isEmpty()) {
            tvSelectedArea.setText(areaList.get(0).getAreaName());
            loadTablesByArea(areaList.get(0).getId());
        }
    }

    private void loadTablesByArea(int areaId) {
        apiService.getTablesByArea(areaId).enqueue(new Callback<List<TableModel>>() {
            @Override
            public void onResponse(Call<List<TableModel>> call, Response<List<TableModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tableList = response.body();
                    tableAdapter.setTables(tableList);
                    updateTableCounts();
                } else {
                    tableAdapter.setTables(new ArrayList<>());
                    updateTableCounts();
                    Toast.makeText(SoDobanActivity.this, "Không có bàn ăn nào trong khu vực này", Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<List<TableModel>> call, Throwable t) {
                Toast.makeText(SoDobanActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTableCounts() {
        int empty = 0;
        int occupied = 0;
        if (tableList != null) {
            for (TableModel table : tableList) {
                if (table.isOccupied()) {
                    occupied++;
                } else {
                    empty++;
                }
            }
        }
        if (tvEmptyCount != null) {
            tvEmptyCount.setText("Trống (" + empty + ")");
        }
        if (tvOccupiedCount != null) {
            tvOccupiedCount.setText("Có khách (" + occupied + ")");
        }
    }

    private void setupClickListeners() {
        // ---- Bottom Navigation ----
        navOrder.setOnClickListener(v -> {
            startActivity(new Intent(this, DanhSachOrderActivity.class));
            overridePendingTransition(0, 0);
        });

        navSoDo.setOnClickListener(v -> {
            /* Đang ở trang này rồi */ });

        navTienIch.setOnClickListener(v -> {
            startActivity(new Intent(this, TienIchActivity.class));
            overridePendingTransition(0, 0);
        });
    }

    private void openLapOrder(String tableName, int tableId) {
        Intent intent = new Intent(this, LapOrderActivity.class);
        intent.putExtra("TABLE_NAME", tableName);
        intent.putExtra("TABLE_ID", tableId);
        startActivity(intent);
    }

    private void openThongTinBan(String tableName, int tableId) {
        Intent intent = new Intent(this, ThongTinBanActivity.class);
        intent.putExtra("TABLE_NAME", tableName);
        intent.putExtra("TABLE_ID", tableId);
        startActivity(intent);
    }
}
