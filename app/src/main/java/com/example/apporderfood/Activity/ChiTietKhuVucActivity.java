package com.example.apporderfood.Activity;

import android.os.Bundle;
import android.view.View;
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
import com.example.apporderfood.adapter.TableInAreaAdapter;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.Area;
import com.example.apporderfood.model.TableModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChiTietKhuVucActivity extends AppCompatActivity {

    private TextView tvAreaName, tvAreaDesc, tvAreaStatus;
    private TextView tvTotalTables, tvEmptyTables, tvOccupiedTables;
    private RecyclerView rvTablesInArea;
    private ProgressBar pbLoading;
    private View llEmptyState;

    private TableInAreaAdapter adapter;
    private List<TableModel> tableList = new ArrayList<>();
    private Area area;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chi_tiet_khu_vuc);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Nhận Area từ Intent
        area = (Area) getIntent().getSerializableExtra("AREA_DATA");
        if (area == null) {
            Toast.makeText(this, "Không tìm thấy khu vực!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        bindAreaInfo();
        setupRecyclerView();
        loadTables();
    }

    private void initViews() {
        tvAreaName   = findViewById(R.id.tvAreaName);
        tvAreaDesc   = findViewById(R.id.tvAreaDesc);
        tvAreaStatus = findViewById(R.id.tvAreaStatus);
        tvTotalTables     = findViewById(R.id.tvTotalTables);
        tvEmptyTables = findViewById(R.id.tvEmptyTables);
        tvOccupiedTables = findViewById(R.id.tvOccupiedTables);
        rvTablesInArea = findViewById(R.id.rvTablesInArea);
        pbLoading         = findViewById(R.id.pbLoading);
        llEmptyState      = findViewById(R.id.llEmptyState);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void bindAreaInfo() {
        tvAreaName.setText(area.getAreaName());
        int count = area.getItemCount() != null ? area.getItemCount() : 0;
        tvAreaDesc.setText(count + " bàn");
        tvAreaDesc.setVisibility(View.VISIBLE);

        boolean active = area.getIsActive() == null || area.getIsActive();
        if (active) {
            tvAreaStatus.setText("HOẠT ĐỘNG");
            tvAreaStatus.setBackgroundResource(R.drawable.bg_status_available);
            tvAreaStatus.setTextColor(getColor(android.R.color.holo_green_dark));
        } else {
            tvAreaStatus.setText("TẠM ẨN");
            tvAreaStatus.setBackgroundResource(R.drawable.bg_status_paused);
            tvAreaStatus.setTextColor(getColor(android.R.color.holo_orange_dark));
        }
    }

    private void setupRecyclerView() {
        adapter = new TableInAreaAdapter(tableList);
        rvTablesInArea.setLayoutManager(new LinearLayoutManager(this));
        rvTablesInArea.setAdapter(adapter);
    }

    private void loadTables() {
        if (area.getId() == null) return;
        pbLoading.setVisibility(View.VISIBLE);
        rvTablesInArea.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.GONE);

        // Get resId from preferences to call getAllTablesByRestaurant
        int resId = getSharedPreferences("ZappySession", MODE_PRIVATE).getInt("RES_ID", -1);
        if (resId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin nhà hàng!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ZappyApiService api = RetrofitClient.getApiService();
        api.getAllTablesByRestaurant(resId).enqueue(new Callback<List<TableModel>>() {
            @Override
            public void onResponse(Call<List<TableModel>> call, Response<List<TableModel>> response) {
                pbLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    tableList.clear();
                    // Filter tables by this area
                    for (TableModel t : response.body()) {
                        if (t.getArea() != null && t.getArea().getId() != null && t.getArea().getId().equals(area.getId())) {
                            tableList.add(t);
                        }
                    }
                    adapter.setItems(tableList);
                    updateStats();
                    if (tableList.isEmpty()) {
                        llEmptyState.setVisibility(View.VISIBLE);
                        rvTablesInArea.setVisibility(View.GONE);
                    } else {
                        rvTablesInArea.setVisibility(View.VISIBLE);
                        llEmptyState.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(ChiTietKhuVucActivity.this,
                            "Lỗi tải danh sách bàn: " + response.code(), Toast.LENGTH_SHORT).show();
                    llEmptyState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<TableModel>> call, Throwable t) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(ChiTietKhuVucActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                llEmptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateStats() {
        int total = tableList.size();
        int empty = 0;
        int occupied = 0;

        for (TableModel item : tableList) {
            // Chỉ bàn isOccupied=true mới tính là có khách
            // Bàn "ĐANG KHÓA" tính riêng, không count vào empty hay occupied
            String status = item.getStatus();
            boolean locked = "ĐANG KHÓA".equals(status);
            if (!locked) {
                if (item.isOccupied()) {
                    occupied++;
                } else {
                    empty++;
                }
            }
        }

        tvTotalTables.setText(String.valueOf(total));
        tvEmptyTables.setText(String.valueOf(empty));
        tvOccupiedTables.setText(String.valueOf(occupied));
    }
}
