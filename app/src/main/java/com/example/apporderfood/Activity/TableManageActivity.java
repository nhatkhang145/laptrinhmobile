package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.R;
import com.example.apporderfood.adapter.TableManageAdapter;
import com.example.apporderfood.model.Area;
import com.example.apporderfood.model.TableModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TableManageActivity extends AppCompatActivity implements TableManageAdapter.OnTableItemClickListener {

    private RecyclerView rvTableList;
    private TableManageAdapter adapter;
    private MaterialButton btnAddTable;
    private ProgressBar pbLoading;
    private TextView tvEmptyState;
    private TextView tvTotalTables, tvActiveTables, tvLockedTables;

    private final ActivityResultLauncher<Intent> addTableLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadTables();
                    Toast.makeText(this, "Đã cập nhật danh sách bàn", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_ban);

        initViews();
        setupRecyclerView();
        loadTables();
        setupListeners();
        setupBottomNav();
    }

    private void initViews() {
        rvTableList = findViewById(R.id.rvTableList);
        btnAddTable = findViewById(R.id.btn_add_table);
        pbLoading = findViewById(R.id.pbTableLoading);
        tvEmptyState = findViewById(R.id.tvTableEmptyState);
        
        tvTotalTables = findViewById(R.id.tvTotalTables);
        tvActiveTables = findViewById(R.id.tvActiveTables);
        tvLockedTables = findViewById(R.id.tvLockedTables);
    }

    private void setupRecyclerView() {
        rvTableList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadTables() {
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        rvTableList.setVisibility(View.GONE);

        // Giả lập tải dữ liệu từ API
        new android.os.Handler().postDelayed(() -> {
            List<TableModel> tableList = new ArrayList<>();
            tableList.add(createMockTable(1, "B01", "HOẠT ĐỘNG", "Tầng 1", 4));
            tableList.add(createMockTable(2, "VIP01", "ĐANG KHÓA", "Phòng VIP", 10));
            tableList.add(createMockTable(3, "B05", "BẢO TRÌ", "Sân vườn", 2));
            tableList.add(createMockTable(4, "B02", "HOẠT ĐỘNG", "Tầng 1", 4));
            tableList.add(createMockTable(5, "B03", "HOẠT ĐỘNG", "Tầng 2", 6));

            if (pbLoading != null) pbLoading.setVisibility(View.GONE);
            
            if (tableList.isEmpty()) {
                if (tvEmptyState != null) tvEmptyState.setVisibility(View.VISIBLE);
                rvTableList.setVisibility(View.GONE);
            } else {
                if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
                rvTableList.setVisibility(View.VISIBLE);
                adapter = new TableManageAdapter(tableList, this);
                rvTableList.setAdapter(adapter);
                updateStats(tableList);
            }
        }, 800);
    }

    private TableModel createMockTable(int id, String name, String status, String areaName, int seats) {
        TableModel table = new TableModel();
        table.setId(id);
        table.setTableName(name);
        table.setStatus(status);
        table.setArea(new Area(null, areaName));
        table.setSeats(seats);
        table.setOccupied("ĐANG KHÓA".equals(status));
        return table;
    }

    private void updateStats(List<TableModel> list) {
        int total = list.size();
        int active = 0;
        int locked = 0;
        for (TableModel item : list) {
            String status = item.getStatus() != null ? item.getStatus() : "HOẠT ĐỘNG";
            if ("HOẠT ĐỘNG".equals(status)) active++;
            if ("ĐANG KHÓA".equals(status) || "BẢO TRÌ".equals(status)) locked++;
        }
        if (tvTotalTables != null) tvTotalTables.setText(String.valueOf(total));
        if (tvActiveTables != null) tvActiveTables.setText(String.valueOf(active));
        if (tvLockedTables != null) tvLockedTables.setText(String.format(Locale.getDefault(), "%02d", locked));
    }

    private void setupListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        if (btnAddTable != null) {
            btnAddTable.setOnClickListener(v -> {
                Intent intent = new Intent(this, ThemBanMoiActivity.class);
                addTableLauncher.launch(intent);
            });
        }
    }

    @Override
    public void onEditClick(TableModel item) {
        Intent intent = new Intent(this, ThemBanMoiActivity.class);
        intent.putExtra("IS_EDIT", true);
        intent.putExtra("TABLE_ID", item.getTableName());
        if (item.getArea() != null) {
            intent.putExtra("TABLE_AREA", item.getArea().getAreaName());
        }
        intent.putExtra("TABLE_SEATS", item.getSeats() != null ? item.getSeats() : 4);
        addTableLauncher.launch(intent);
    }

    @Override
    public void onMoreClick(TableModel item, View view) {
        Toast.makeText(this, "Tùy chọn: " + item.getTableName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(TableModel item) {
        Toast.makeText(this, "Chi tiết: " + item.getTableName(), Toast.LENGTH_SHORT).show();
    }

    private void setupBottomNav() {
        View navOrder = findViewById(R.id.navOrder);
        View navSoDo = findViewById(R.id.navSoDo);
        View navTienIch = findViewById(R.id.navTienIch);

        if (navOrder != null) {
            navOrder.setOnClickListener(v -> {
                startActivity(new Intent(this, DanhSachOrderActivity.class));
                overridePendingTransition(0, 0);
            });
        }
        if (navSoDo != null) {
            navSoDo.setOnClickListener(v -> {
                startActivity(new Intent(this, SoDobanActivity.class));
                overridePendingTransition(0, 0);
            });
        }
        if (navTienIch != null) {
            navTienIch.setOnClickListener(v -> {
                startActivity(new Intent(this, TienIchActivity.class));
                overridePendingTransition(0, 0);
                finish();
            });
        }
    }
}
