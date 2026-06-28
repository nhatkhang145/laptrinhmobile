package com.example.apporderfood.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apporderfood.R;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.Area;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThemKhuVucActivity extends AppCompatActivity {

    private EditText edtName;
    private TextView tvTitle, tvPreviewName, tvPreviewStatus, tvStatusActive, tvStatusHidden;
    private MaterialButton btnSave;
    private boolean isEditMode = false;
    private boolean isActive = true;
    private int resId = -1;
    private Integer editingAreaId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_khu_vuc);

        SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        resId = prefs.getInt("RES_ID", -1);

        initViews();
        checkEditMode();
        setupListeners();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_form_title);
        edtName = findViewById(R.id.edt_Area_name);
        tvPreviewName = findViewById(R.id.tv_preview_name);
        tvPreviewStatus = findViewById(R.id.tv_preview_status);
        tvStatusActive = findViewById(R.id.tv_status_active);
        tvStatusHidden = findViewById(R.id.tv_status_hidden);
        btnSave = findViewById(R.id.btn_save);
    }

    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("IS_EDIT", false)) {
            isEditMode = true;
            tvTitle.setText("Chỉnh sửa khu vực");
            if (btnSave != null)
                btnSave.setText("CẬP NHẬT");

            Area area = (Area) intent.getSerializableExtra("Area_DATA");
            if (area != null) {
                editingAreaId = area.getId();
                edtName.setText(area.getAreaName());
                tvPreviewName.setText(area.getAreaName());
                boolean active = area.getIsActive() != null ? area.getIsActive() : true;
                updateStatusUI(active);
            } else {
                updateStatusUI(true);
            }
        } else {
            updateStatusUI(true);
        }
    }

    private void setupListeners() {
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());

        edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvPreviewName.setText(s.length() > 0 ? s.toString() : "Tên khu vực");
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        tvStatusActive.setOnClickListener(v -> updateStatusUI(true));
        tvStatusHidden.setOnClickListener(v -> updateStatusUI(false));
        btnSave.setOnClickListener(v -> validateAndSave());
    }

    private void updateStatusUI(boolean active) {
        this.isActive = active;
        if (active) {
            tvStatusActive.setBackgroundResource(R.drawable.bg_tab_active_dark);
            tvStatusActive.setTextColor(getColor(R.color.white));
            tvStatusHidden.setBackgroundResource(R.drawable.bg_search_bar);
            tvStatusHidden.setTextColor(getColor(R.color.text_secondary));

            tvPreviewStatus.setText("● HOẠT ĐỘNG");
            tvPreviewStatus.setTextColor(getColor(android.R.color.holo_green_dark));
        } else {
            tvStatusHidden.setBackgroundResource(R.drawable.bg_tab_active_dark);
            tvStatusHidden.setTextColor(getColor(R.color.white));
            tvStatusActive.setBackgroundResource(R.drawable.bg_search_bar);
            tvStatusActive.setTextColor(getColor(R.color.text_secondary));

            tvPreviewStatus.setText("○ TẠM ẨN");
            tvPreviewStatus.setTextColor(getColor(android.R.color.holo_orange_dark));
        }
    }

    private void validateAndSave() {
        String name = edtName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên khu vực", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("ĐANG XỬ LÝ...");

        Map<String, Object> data = new HashMap<>();
        data.put("areaName", name);
        data.put("resId", resId);
        data.put("isActive", isActive);

        ZappyApiService api = RetrofitClient.getApiService();
        if (isEditMode && editingAreaId != null) {
            api.updateArea(editingAreaId, data).enqueue(new Callback<Area>() {
                @Override
                public void onResponse(Call<Area> call, Response<Area> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ThemKhuVucActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        btnSave.setEnabled(true);
                        btnSave.setText("CẬP NHẬT");
                        Toast.makeText(ThemKhuVucActivity.this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Area> call, Throwable t) {
                    btnSave.setEnabled(true);
                    btnSave.setText("CẬP NHẬT");
                    Toast.makeText(ThemKhuVucActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            api.createArea(data).enqueue(new Callback<Area>() {
                @Override
                public void onResponse(Call<Area> call, Response<Area> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ThemKhuVucActivity.this, "Thêm mới thành công!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        btnSave.setEnabled(true);
                        btnSave.setText("LƯU KHU VỰC");
                        Toast.makeText(ThemKhuVucActivity.this, "Thêm thất bại!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Area> call, Throwable t) {
                    btnSave.setEnabled(true);
                    btnSave.setText("LƯU KHU VỰC");
                    Toast.makeText(ThemKhuVucActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
