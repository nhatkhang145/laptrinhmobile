package com.example.apporderfood.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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
import com.example.apporderfood.adapter.AreaManageAdapter;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.Area;
import com.example.apporderfood.model.TableModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AreaManageActivity extends AppCompatActivity
        implements AreaManageAdapter.OnAreaItemClickListener {

    private RecyclerView rvAreaList;
    private AreaManageAdapter adapter;
    private FloatingActionButton fabAddArea;
    private ProgressBar pbLoading;
    private TextView tvEmptyState;
    private TextView tvTotalAreas, tvActiveAreas, tvHiddenAreas;
    private EditText etSearch;

    private int resId = -1;

    private final ActivityResultLauncher<Intent> addAreaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadAreas();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quan_ly_khu_vuc);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        resId = prefs.getInt("RES_ID", -1);

        initViews();
        setupRecyclerView();
        loadAreas();
        setupListeners();
        setupBottomNav();
    }

    private void initViews() {
        rvAreaList     = findViewById(R.id.rvAreaList);
        fabAddArea     = findViewById(R.id.fab_add_Area);
        pbLoading      = findViewById(R.id.pbAreaLoading);
        tvEmptyState   = findViewById(R.id.tvAreaEmptyState);
        tvTotalAreas   = findViewById(R.id.tvTotalAreas);
        tvActiveAreas  = findViewById(R.id.tvActiveAreas);
        tvHiddenAreas  = findViewById(R.id.tvHiddenAreas);
        etSearch       = findViewById(R.id.etSearch);
    }

    private void setupRecyclerView() {
        if (rvAreaList != null) {
            rvAreaList.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    private void loadAreas() {
        if (resId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin nhà hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (rvAreaList != null) rvAreaList.setVisibility(View.GONE);
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);

        ZappyApiService api = RetrofitClient.getApiService();
        api.getAreas(resId).enqueue(new Callback<List<Area>>() {
            @Override
            public void onResponse(Call<List<Area>> call, Response<List<Area>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Area> AreaList = response.body();
                    if (AreaList.isEmpty()) {
                        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                        if (tvEmptyState != null) tvEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        api.getAllTablesByRestaurant(resId).enqueue(new Callback<List<TableModel>>() {
                            @Override
                            public void onResponse(Call<List<TableModel>> call, Response<List<TableModel>> responseMenu) {
                                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                                if (responseMenu.isSuccessful() && responseMenu.body() != null) {
                                    List<TableModel> tables = responseMenu.body();
                                    for (Area cat : AreaList) {
                                        int count = 0;
                                        StringBuilder names = new StringBuilder();
                                        for (TableModel item : tables) {
                                            if (item.getArea() != null && item.getArea().getId() != null && item.getArea().getId().equals(cat.getId())) {
                                                count++;
                                                if (names.length() > 0) names.append(", ");
                                                names.append(item.getTableName());
                                            }
                                        }
                                        cat.setItemCount(count);
                                        cat.setTableNames(names.toString());
                                    }
                                }
                                displayAreas(AreaList);
                            }

                            @Override
                            public void onFailure(Call<List<TableModel>> call, Throwable t) {
                                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                                displayAreas(AreaList);
                            }
                        });
                    }
                } else {
                    if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                    Toast.makeText(AreaManageActivity.this, "Lỗi tải khu vực", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Area>> call, Throwable t) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                Toast.makeText(AreaManageActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayAreas(List<Area> AreaList) {
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
        adapter = new AreaManageAdapter(AreaList, AreaManageActivity.this);
        if (rvAreaList != null) {
            rvAreaList.setVisibility(View.VISIBLE);
            rvAreaList.setAdapter(adapter);
        }
        updateStats(AreaList);
    }

    private void updateStats(List<Area> list) {
        int total = list.size();
        int active = 0;
        int hidden = 0;
        for (Area item : list) {
            if (item.getIsActive() == null || item.getIsActive()) active++;
            else hidden++;
        }
        if (tvTotalAreas != null) tvTotalAreas.setText(String.valueOf(total));
        if (tvActiveAreas != null) tvActiveAreas.setText(String.valueOf(active));
        if (tvHiddenAreas != null) tvHiddenAreas.setText(String.valueOf(hidden));
    }

    private void updateStatsFromAdapter() {
        if (adapter == null) return;
        loadAreas();
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (fabAddArea != null) {
            fabAddArea.setOnClickListener(v -> {
                Intent intent = new Intent(this, ThemKhuVucActivity.class);
                addAreaLauncher.launch(intent);
            });
        }

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) adapter.filter(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    @Override
    public void onEditClick(Area item) {
        Intent intent = new Intent(this, ThemKhuVucActivity.class);
        intent.putExtra("IS_EDIT", true);
        intent.putExtra("Area_DATA", item);
        addAreaLauncher.launch(intent);
    }

    @Override
    public void onStatusToggleClick(Area item, int position) {
        if (item.getId() == null) return;

        boolean currentlyActive = item.getIsActive() == null || item.getIsActive();
        boolean newStatus = !currentlyActive;
        String newStatusLabel = currentlyActive ? "TẠM ẨN" : "HOẠT ĐỘNG";

        ZappyApiService api = RetrofitClient.getApiService();
        
        api.toggleAreaStatus(item.getId()).enqueue(new Callback<Map>() {
            @Override
            public void onResponse(Call<Map> call, Response<Map> response) {
                if (response.isSuccessful()) {
                    item.setIsActive(newStatus);
                    if (adapter != null) adapter.updateItemStatus(position, newStatus);
                    updateStatsFromAdapter();
                    Toast.makeText(AreaManageActivity.this,
                            "\"" + item.getAreaName() + "\" → " + newStatusLabel,
                            Toast.LENGTH_SHORT).show();
                } else if (response.code() == 400) {
                     Toast.makeText(AreaManageActivity.this,
                            "Không thể ẩn khu vực đang có bàn có khách!",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AreaManageActivity.this,
                            "Cập nhật thất bại (lỗi " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map> call, Throwable t) {
                Toast.makeText(AreaManageActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteClick(Area item) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa khu vực")
                .setMessage("Bạn có chắc muốn xóa khu vực \"" + item.getAreaName() + "\"?\n"
                        + "Các bàn trong khu vực này sẽ bị ảnh hưởng.")
                .setPositiveButton("Xóa", (dialog, which) -> performDelete(item))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performDelete(Area item) {
        if (item.getId() == null) return;

        ZappyApiService api = RetrofitClient.getApiService();
        api.deleteArea(item.getId()).enqueue(new Callback<Map>() {
            @Override
            public void onResponse(Call<Map> call, Response<Map> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AreaManageActivity.this,
                            "Đã xóa khu vực: " + item.getAreaName(), Toast.LENGTH_SHORT).show();
                    if (adapter != null) {
                        adapter.removeItem(item);
                        loadAreas();
                    }
                } else if (response.code() == 409 || response.code() == 400) {
                    Toast.makeText(AreaManageActivity.this,
                            "Không thể xóa: khu vực đang có bàn hoặc đang được sử dụng",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AreaManageActivity.this,
                            "Xóa thất bại (lỗi " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map> call, Throwable t) {
                Toast.makeText(AreaManageActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(Area item) {
        Intent intent = new Intent(this, ChiTietKhuVucActivity.class);
        intent.putExtra("AREA_DATA", item);
        startActivity(intent);
    }

    private void setupBottomNav() {
        View navOrder  = findViewById(R.id.navOrder);
        View navSoDo   = findViewById(R.id.navSoDo);
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
