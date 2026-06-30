package com.example.apporderfood.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.R;
import com.example.apporderfood.adapter.TableManageAdapter;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.TableModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * TableManageActivity (Màn hình Quản lý Bàn/Khu vực)
 * Nhiệm vụ chính:
 * - Hiển thị danh sách tất cả các bàn trong nhà hàng.
 * - Thống kê số lượng bàn (tổng số, đang hoạt động, đang khóa).
 * - Cung cấp các thao tác: Thêm mới bàn, chỉnh sửa thông tin, xóa bàn,
 *   và bật/tắt trạng thái bàn (hoạt động / đang khóa).
 */
public class TableManageActivity extends AppCompatActivity implements TableManageAdapter.OnTableItemClickListener {

    private RecyclerView rvTableList;
    private TableManageAdapter adapter;
    private FloatingActionButton btnAddTable;
    private ProgressBar pbLoading;
    private TextView tvEmptyState;
    private TextView tvTotalTables, tvActiveTables, tvLockedTables;

    private int resId = -1;

    private final ActivityResultLauncher<Intent> addTableLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadTables(); // Tự động làm mới danh sách khi thêm/sửa thành công
                    Toast.makeText(this, "Đã cập nhật danh sách bàn", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_ban);

        // Lấy resId của nhà hàng hiện tại từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        resId = prefs.getInt("RES_ID", -1);

        initViews();
        setupRecyclerView();
        
        // Tải danh sách bàn từ server
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
        if (rvTableList != null) {
            rvTableList.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    /**
     * Gọi API lấy danh sách các bàn thuộc nhà hàng.
     */
    private void loadTables() {
        if (resId == -1) return;

        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (rvTableList != null) rvTableList.setVisibility(View.GONE);
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);

        ZappyApiService api = RetrofitClient.getApiService();
        api.getAllTablesByRestaurant(resId).enqueue(new Callback<List<TableModel>>() {
            @Override
            public void onResponse(Call<List<TableModel>> call, Response<List<TableModel>> response) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<TableModel> tableList = response.body();
                    if (tableList.isEmpty()) {
                        if (tvEmptyState != null) tvEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        adapter = new TableManageAdapter(tableList, TableManageActivity.this);
                        if (rvTableList != null) {
                            rvTableList.setVisibility(View.VISIBLE);
                            rvTableList.setAdapter(adapter);
                        }
                        updateStats(tableList);
                    }
                } else {
                    Toast.makeText(TableManageActivity.this, "Không thể lấy dữ liệu bàn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TableModel>> call, Throwable t) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                Toast.makeText(TableManageActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Cập nhật thông số thống kê số lượng bàn.
     */
    private void updateStats(List<TableModel> list) {
        int total = list.size();
        int active = 0;
        int locked = 0;
        for (TableModel item : list) {
            String status = item.getStatus();
            // Bàn được coi là "hoạt động" nếu status null (default) hoặc bằng "HOẠT ĐỘNG"
            if (status == null || "HOẠT ĐỘNG".equals(status)) active++;
            else locked++; // Các trạng thái còn lại như "ĐANG KHÓA", "BẢO TRÌ"
        }
        
        // Hiển thị lên giao diện
        if (tvTotalTables != null) tvTotalTables.setText(String.valueOf(total));
        if (tvActiveTables != null) tvActiveTables.setText(String.valueOf(active));
        if (tvLockedTables != null) tvLockedTables.setText(String.format(Locale.getDefault(), "%02d", locked));
    }

    /** Cập nhật stats sau khi toggle status mà không cần reload API ngay lập tức */
    private void updateStatsFromAdapter() {
        if (adapter != null && adapter.getTableList() != null) {
            updateStats(adapter.getTableList());
        }
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

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
        intent.putExtra("TABLE_DATA", item); // Truyền toàn bộ object (đã implement Serializable)
        addTableLauncher.launch(intent);
    }

    /**
     * Xử lý khi nhấn nút bật/tắt trạng thái hoạt động của bàn.
     */
    @Override
    public void onStatusToggleClick(TableModel item, int position) {
        if (item.getId() == null) return;

        // Đảo trạng thái: HOẠT ĐỘNG <-> ĐANG KHÓA
        boolean currentlyActive = item.getStatus() == null || "HOẠT ĐỘNG".equals(item.getStatus());
        String newStatus = currentlyActive ? "ĐANG KHÓA" : "HOẠT ĐỘNG";

        ZappyApiService api = RetrofitClient.getApiService();
        Map<String, Object> body = new HashMap<>();
        body.put("tableName", item.getTableName());
        body.put("status", newStatus);

        api.updateTable(item.getId(), body).enqueue(new Callback<TableModel>() {
            @Override
            public void onResponse(Call<TableModel> call, Response<TableModel> response) {
                if (response.isSuccessful()) {
                    // Update local list
                    item.setStatus(newStatus);
                    if (adapter != null) adapter.updateItemStatus(position, newStatus);
                    updateStatsFromAdapter();
                    Toast.makeText(TableManageActivity.this,
                            "Bàn \"" + item.getTableName() + "\" → " + newStatus,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(TableManageActivity.this,
                            "Cập nhật thất bại (lỗi " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TableModel> call, Throwable t) {
                Toast.makeText(TableManageActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Hiển thị cảnh báo trước khi xóa bàn.
     */
    @Override
    public void onDeleteClick(TableModel item) {
        if (item.getId() == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Xóa bàn")
                .setMessage("Bạn có chắc chắn muốn xóa bàn \"" + item.getTableName() + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> performDelete(item))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performDelete(TableModel item) {
        ZappyApiService api = RetrofitClient.getApiService();
        api.deleteTable(item.getId()).enqueue(new Callback<Map>() {
            @Override
            public void onResponse(Call<Map> call, Response<Map> response) {
                if (response.isSuccessful()) {
                    if (adapter != null) {
                        adapter.removeItem(item);
                        updateStatsFromAdapter();
                        
                        if (adapter.getTableList().isEmpty() && tvEmptyState != null) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            if (rvTableList != null) rvTableList.setVisibility(View.GONE);
                        }
                    }
                    Toast.makeText(TableManageActivity.this, "Đã xóa bàn", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 409 || response.code() == 400) {
                    Toast.makeText(TableManageActivity.this,
                            "Không thể xóa bàn đã có hóa đơn/khách", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(TableManageActivity.this,
                            "Xóa thất bại (lỗi " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map> call, Throwable t) {
                Toast.makeText(TableManageActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(TableModel item) {
        Toast.makeText(this, "Xem chi tiết: " + item.getTableName(), Toast.LENGTH_SHORT).show();
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
