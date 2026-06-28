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
import androidx.core.content.ContextCompat;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.LinearLayout;

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
    private LinearLayout llAreaChips;
    private TextView tvStatusToggle;
    private boolean isToggleOn = true; // mặc định: bật (màu)

    private String selectedAreaName = "";
    private Integer selectedAreaId = null;  // ID thực của khu vực
    private boolean isEditMode = false;
    private boolean areaExplicitlyChanged = false; // Chỉ đổi khu vực khi user bấm chip
    private TableModel editingTable = null;

    // Danh sách khu vực từ API
    private List<Area> areaList;

    private int resId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_them_ban_moi);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
        llAreaChips = findViewById(R.id.ll_area_chips);
        tvStatusToggle = findViewById(R.id.tvStatusToggle);
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
        if (areaList == null || areaList.isEmpty() || llAreaChips == null) return;
        llAreaChips.removeAllViews();

        for (int i = 0; i < areaList.size(); i++) {
            Area area = areaList.get(i);
            TextView chip = new TextView(this);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (i > 0) {
                params.setMarginStart((int) (8 * getResources().getDisplayMetrics().density));
            }
            chip.setLayoutParams(params);
            
            chip.setText(area.getAreaName());
            chip.setTextSize(12);
            chip.setPadding(
                    (int) (16 * getResources().getDisplayMetrics().density),
                    (int) (8 * getResources().getDisplayMetrics().density),
                    (int) (16 * getResources().getDisplayMetrics().density),
                    (int) (8 * getResources().getDisplayMetrics().density)
            );
            
            // So sánh bằng ID để tránh nhầm khu vực trùng tên
            boolean isSelected = selectedAreaId != null && selectedAreaId.equals(area.getId());
            if (isSelected) {
                chip.setBackgroundResource(R.drawable.bg_tab_active_dark);
                chip.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                chip.setBackgroundResource(R.drawable.bg_tab_inactive);
                chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            }
            
            chip.setClickable(true);
            chip.setFocusable(true);
            
            chip.setOnClickListener(v -> {
                selectArea(area.getAreaName(), area.getId());
                areaExplicitlyChanged = true; // đánh dấu user đã tự chọn khu vực
                updateChipsWithAreas();
            });
            
            llAreaChips.addView(chip);
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
                // Pre-fill toggle theo trạng thái bàn hiện tại
                if (editingTable.getStatus() != null) {
                    isToggleOn = !"ĐANG KHÓA".equals(editingTable.getStatus());
                    updateToggleUI();
                }
            }
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Toggle visual: bật = màu primary, tắt = trắng có viền
        if (tvStatusToggle != null) {
            tvStatusToggle.setOnClickListener(v -> {
                isToggleOn = !isToggleOn;
                updateToggleUI();
            });
        }

        btnConfirm.setOnClickListener(v -> validateAndSave());
    }

    private void selectArea(String name, Integer id) {
        selectedAreaName = name;
        selectedAreaId = id;
        tvSelectedArea.setText(selectedAreaName);
        tvSelectedArea.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
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

    private void updateToggleUI() {
        if (tvStatusToggle == null) return;
        if (isToggleOn) {
            // Bật = HOẠT ĐỘNG: nền màu primary, chữ trắng
            tvStatusToggle.setBackgroundResource(R.drawable.bg_tab_active_dark);
            tvStatusToggle.setTextColor(ContextCompat.getColor(this, R.color.white));
            tvStatusToggle.setText("● HOẠT ĐỘNG");
        } else {
            // Tắt = ĐANG KHÓA: nền trắng, viền xám, chữ mờ
            tvStatusToggle.setBackgroundResource(R.drawable.bg_tab_inactive);
            tvStatusToggle.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            tvStatusToggle.setText("○ ĐANG KHÓA");
        }
    }

    private void createTable(int areaId, String tableName) {
        String seatsStr = edtTableSeats.getText().toString().trim();
        int seats = seatsStr.isEmpty() ? 0 : Integer.parseInt(seatsStr);

        Map<String, Object> body = new HashMap<>();
        body.put("areaId", areaId);
        body.put("tableName", tableName);
        body.put("seats", seats);
        body.put("status", isToggleOn ? "HOẠT ĐỘNG" : "ĐANG KHÓA");

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
        String seatsStr = edtTableSeats.getText().toString().trim();
        int seats = seatsStr.isEmpty() ? 0 : Integer.parseInt(seatsStr);

        Map<String, Object> body = new HashMap<>();
        body.put("tableName", tableName);
        body.put("seats", seats);
        body.put("status", isToggleOn ? "HOẠT ĐỘNG" : "ĐANG KHÓA");
        // Chỉ đổi khu vực khi user bấm chip - tránh đổi nhầm do trùng tên
        if (areaExplicitlyChanged && selectedAreaId != null) {
            body.put("areaId", selectedAreaId);
        }

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
