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
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

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
        pbLoading.setVisibility(View.VISIBLE);
        rvTableList.setVisibility(View.GONE);

        // Giả lập tải dữ liệu từ API
        new android.os.Handler().postDelayed(() -> {
            List<TableManageAdapter.TableItem> tableList = new ArrayList<>();
            tableList.add(new TableManageAdapter.TableItem("B01", "HOẠT ĐỘNG", "Tầng 1", 4, "12/10/2023"));
            tableList.add(new TableManageAdapter.TableItem("VIP01", "ĐANG KHÓA", "Phòng VIP", 10, "15/10/2023"));
            tableList.add(new TableManageAdapter.TableItem("B05", "BẢO TRÌ", "Sân vườn", 2, "20/10/2023"));
            tableList.add(new TableManageAdapter.TableItem("B02", "HOẠT ĐỘNG", "Tầng 1", 4, "12/10/2023"));
            tableList.add(new TableManageAdapter.TableItem("B03", "HOẠT ĐỘNG", "Tầng 2", 6, "14/10/2023"));

            pbLoading.setVisibility(View.GONE);
            
            if (tableList.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                rvTableList.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                rvTableList.setVisibility(View.VISIBLE);
                adapter = new TableManageAdapter(tableList, this);
                rvTableList.setAdapter(adapter);
                updateStats(tableList);
            }
        }, 800);
    }

    private void updateStats(List<TableManageAdapter.TableItem> list) {
        int total = list.size();
        int active = 0;
        int locked = 0;
        for (TableManageAdapter.TableItem item : list) {
            if ("HOẠT ĐỘNG".equals(item.getStatus())) active++;
            if ("ĐANG KHÓA".equals(item.getStatus()) || "BẢO TRÌ".equals(item.getStatus())) locked++;
        }
        tvTotalTables.setText(String.valueOf(total));
        tvActiveTables.setText(String.valueOf(active));
        tvLockedTables.setText(String.format("%02d", locked));
    }

    private void setupListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnAddTable.setOnClickListener(v -> {
            Intent intent = new Intent(this, ThemBanMoiActivity.class);
            addTableLauncher.launch(intent);
        });
    }

    @Override
    public void onEditClick(TableManageAdapter.TableItem item) {
        Intent intent = new Intent(this, ThemBanMoiActivity.class);
        intent.putExtra("IS_EDIT", true);
        intent.putExtra("TABLE_ID", item.getTableId());
        intent.putExtra("TABLE_AREA", item.getArea());
        intent.putExtra("TABLE_SEATS", item.getSeats());
        addTableLauncher.launch(intent);
    }

    @Override
    public void onMoreClick(TableManageAdapter.TableItem item, View view) {
        Toast.makeText(this, "Tùy chọn: " + item.getTableId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(TableManageAdapter.TableItem item) {
        Toast.makeText(this, "Chi tiết: " + item.getTableId(), Toast.LENGTH_SHORT).show();
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
