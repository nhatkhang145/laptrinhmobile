package com.example.apporderfood.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.apporderfood.R;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.Area;
import com.example.apporderfood.model.TableModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThemBanMoiActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton btnConfirm;
    private EditText edtTableName, edtTableSeats;
    private TextView tvTitle, tvSelectedArea;
    private TextView chipTang1, chipTang2, chipSanVuon;
    private SwitchMaterial switchStatus;

    private String selectedAreaName = "";
    private Integer selectedAreaId = null;  // ID thực của khu vực
    private boolean isEditMode = false;
    private TableModel editingTable = null;

    // Danh sách khu vực từ API
    private List<Area> areaList;

    private int resId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_ban_moi);

        SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        resId = prefs.getInt("RES_ID", -1);

        initViews();
        loadAreas();         // Load danh sách khu vực từ API
        checkEditMode();
        setupListeners();
    }

    private void initViews() {
        // Tìm title trong Toolbar
        View toolbar = findViewById(R.id.toolbar);
        if (toolbar instanceof androidx.appcompat.widget.Toolbar) {
            for (int i = 0; i < ((androidx.appcompat.widget.Toolbar) toolbar).getChildCount(); i++) {
                View v = ((androidx.appcompat.widget.Toolbar) toolbar).getChildAt(i);
                if (v instanceof TextView && !v.equals(findViewById(R.id.btn_back))) {
                    tvTitle = (TextView) v;
                    break;
                }
            }
        }

        btnBack = findViewById(R.id.btn_back);
        btnConfirm = findViewById(R.id.btn_confirm);
        edtTableName = findViewById(R.id.edt_table_name);
        edtTableSeats = findViewById(R.id.edt_table_seats);
        tvSelectedArea = findViewById(R.id.tv_selected_area);
        chipTang1 = findViewById(R.id.chip_tang1);
        chipTang2 = findViewById(R.id.chip_tang2);
        chipSanVuon = findViewById(R.id.chip_san_vuon);
        switchStatus = findViewById(R.id.switch_status);
    }

    /**
     * Load danh sách khu vực từ API để lấy areaId thực
     */
    private void loadAreas() {
        if (resId == -1) return;

        ZappyApiService api = RetrofitClient.getApiService();
        api.getAreas(resId).enqueue(new Callback<List<Area>>() {
            @Override
            public void onResponse(Call<List<Area>> call, Response<List<Area>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    areaList = response.body();
                    // Cập nhật chip với tên khu vực từ server
                    updateChipsWithAreas();
                }
            }

            @Override
            public void onFailure(Call<List<Area>> call, Throwable t) {
                // Vẫn cho phép nhập thủ công, chỉ cảnh báo
                Toast.makeText(ThemBanMoiActivity.this,
                        "Không tải được danh sách khu vực, kiểm tra kết nối",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Cập nhật các chip theo tên khu vực thực từ server
     */
    private void updateChipsWithAreas() {
        if (areaList == null || areaList.isEmpty()) return;

        // Gán tên từ server vào 3 chip đầu tiên
        if (areaList.size() > 0 && chipTang1 != null) {
            chipTang1.setText(areaList.get(0).getAreaName());
        }
        if (areaList.size() > 1 && chipTang2 != null) {
            chipTang2.setText(areaList.get(1).getAreaName());
        }
        if (areaList.size() > 2 && chipSanVuon != null) {
            chipSanVuon.setText(areaList.get(2).getAreaName());
        }
    }

    /**
     * Tìm areaId theo tên khu vực đã chọn
     */
    private Integer findAreaIdByName(String name) {
        if (areaList == null) return null;
        for (Area area : areaList) {
            if (area.getAreaName() != null && area.getAreaName().equals(name)) {
                return area.getId();
            }
        }
        return null;
    }

    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("IS_EDIT", false)) {
            isEditMode = true;
            editingTable = (TableModel) intent.getSerializableExtra("TABLE_DATA");

            if (tvTitle != null) tvTitle.setText("Chỉnh sửa bàn");
            btnConfirm.setText("CẬP NHẬT");

            if (editingTable != null) {
                edtTableName.setText(editingTable.getTableName());
                if (editingTable.getSeats() != null) {
                    edtTableSeats.setText(String.valueOf(editingTable.getSeats()));
                }
                if (editingTable.getArea() != null) {
                    selectedAreaName = editingTable.getArea().getAreaName();
                    selectedAreaId = editingTable.getArea().getId();
                    tvSelectedArea.setText(selectedAreaName);
                    tvSelectedArea.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                }
            }
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        View.OnClickListener areaClickListener = v -> {
            updateChipSelection((TextView) v);
            selectedAreaName = ((TextView) v).getText().toString();
            selectedAreaId = findAreaIdByName(selectedAreaName);
            tvSelectedArea.setText(selectedAreaName);
            tvSelectedArea.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        };

        chipTang1.setOnClickListener(areaClickListener);
        chipTang2.setOnClickListener(areaClickListener);
        chipSanVuon.setOnClickListener(areaClickListener);

        btnConfirm.setOnClickListener(v -> validateAndSave());
    }

    private void updateChipSelection(TextView selectedChip) {
        resetChips();
        selectedChip.setBackgroundResource(R.drawable.bg_tab_active_dark);
        selectedChip.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    private void resetChips() {
        TextView[] chips = {chipTang1, chipTang2, chipSanVuon};
        for (TextView chip : chips) {
            chip.setBackgroundResource(R.drawable.bg_tab_inactive);
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    private void validateAndSave() {
        String name = edtTableName.getText().toString().trim();
        String seatsStr = edtTableSeats.getText().toString().trim();

        if (name.isEmpty()) {
            edtTableName.setError("Tên bàn không được để trống");
            edtTableName.requestFocus();
            return;
        }

        if (seatsStr.isEmpty()) {
            edtTableSeats.setError("Vui lòng nhập sức chứa");
            edtTableSeats.requestFocus();
            return;
        }

        if (selectedAreaName.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn khu vực", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy areaId - ưu tiên ID đã chọn, nếu null thì tìm lại
        Integer areaId = selectedAreaId != null ? selectedAreaId : findAreaIdByName(selectedAreaName);
        if (areaId == null) {
            Toast.makeText(this, "Không tìm thấy khu vực, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            return;
        }

        setFormEnabled(false);

        if (isEditMode && editingTable != null) {
            // Chế độ sửa: gọi PUT /api/tables/{id}
            updateTable(editingTable.getId(), name);
        } else {
            // Chế độ thêm mới: gọi POST /api/tables
            createTable(areaId, name);
        }
    }

    private void createTable(int areaId, String tableName) {
        Map<String, Object> body = new HashMap<>();
        body.put("areaId", areaId);
        body.put("tableName", tableName);

        ZappyApiService api = RetrofitClient.getApiService();
        api.createTable(body).enqueue(new Callback<TableModel>() {
            @Override
            public void onResponse(Call<TableModel> call, Response<TableModel> response) {
                setFormEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ThemBanMoiActivity.this,
                            "Đã thêm bàn \"" + response.body().getTableName() + "\" thành công!",
                            Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(ThemBanMoiActivity.this,
                            "Thêm bàn thất bại (lỗi " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TableModel> call, Throwable t) {
                setFormEnabled(true);
                Toast.makeText(ThemBanMoiActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTable(int tableId, String tableName) {
        Map<String, Object> body = new HashMap<>();
        body.put("tableName", tableName);

        ZappyApiService api = RetrofitClient.getApiService();
        api.updateTable(tableId, body).enqueue(new Callback<TableModel>() {
            @Override
            public void onResponse(Call<TableModel> call, Response<TableModel> response) {
                setFormEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ThemBanMoiActivity.this,
                            "Đã cập nhật bàn thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(ThemBanMoiActivity.this,
                            "Cập nhật thất bại (lỗi " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TableModel> call, Throwable t) {
                setFormEnabled(true);
                Toast.makeText(ThemBanMoiActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setFormEnabled(boolean enabled) {
        btnConfirm.setEnabled(enabled);
        btnConfirm.setText(enabled ? (isEditMode ? "CẬP NHẬT" : "XÁC NHẬN") : "Đang lưu...");
        edtTableName.setEnabled(enabled);
        edtTableSeats.setEnabled(enabled);
    }
}
