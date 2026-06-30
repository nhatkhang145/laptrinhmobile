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
import com.example.apporderfood.model.Category;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ThemDanhMucActivity (Màn hình Thêm/Chỉnh sửa Danh mục)
 * Nhiệm vụ chính:
 * - Hiển thị form để người dùng nhập thông tin danh mục (Tên, Mô tả, Trạng thái).
 * - Xử lý logic Thêm mới danh mục hoặc Cập nhật danh mục đã có (dựa vào cờ IS_EDIT).
 * - Gửi dữ liệu lên server thông qua API.
 */
public class ThemDanhMucActivity extends AppCompatActivity {

    private EditText edtName, edtDesc;
    private TextView tvTitle, tvPreviewName, tvPreviewStatus, tvStatusActive, tvStatusHidden;
    private MaterialButton btnSave;
    private boolean isEditMode = false;
    private boolean isActive = true; // trạng thái mặc định
    private int resId = -1;
    private Integer editingCategoryId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_danh_muc);

        SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        resId = prefs.getInt("RES_ID", -1);

        initViews();
        checkEditMode();
        setupListeners();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_form_title);
        edtName = findViewById(R.id.edt_category_name);
        edtDesc = findViewById(R.id.edt_description);
        tvPreviewName = findViewById(R.id.tv_preview_name);
        tvPreviewStatus = findViewById(R.id.tv_preview_status);
        tvStatusActive = findViewById(R.id.tv_status_active);
        tvStatusHidden = findViewById(R.id.tv_status_hidden);
        btnSave = findViewById(R.id.btn_save);
    }

    /**
     * Kiểm tra xem màn hình này được mở để Thêm mới hay Chỉnh sửa.
     * Dựa vào dữ liệu "IS_EDIT" truyền qua Intent.
     */
    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("IS_EDIT", false)) {
            isEditMode = true;
            tvTitle.setText("Chỉnh sửa danh mục");
            if (btnSave != null) btnSave.setText("CẬP NHẬT");

            // Nhận Category object từ Intent (dữ liệu của danh mục cần sửa)
            Category cat = (Category) intent.getSerializableExtra("CATEGORY_DATA");
            if (cat != null) {
                editingCategoryId = cat.getId();
                edtName.setText(cat.getCatName());
                edtDesc.setText(cat.getDescription());
                tvPreviewName.setText(cat.getCatName());
                boolean active = cat.getStatus() == null || cat.getStatus() == 1;
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
                tvPreviewName.setText(s.length() > 0 ? s.toString() : "Tên danh mục");
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        tvStatusActive.setOnClickListener(v -> updateStatusUI(true));
        tvStatusHidden.setOnClickListener(v -> updateStatusUI(false));

        if (btnSave != null) btnSave.setOnClickListener(v -> validateAndSave());
    }

    private void updateStatusUI(boolean active) {
        this.isActive = active;
        // Reset styles
        tvStatusActive.setBackgroundResource(R.drawable.bg_search_bar);
        tvStatusActive.setTextColor(getColor(R.color.text_secondary));
        tvStatusHidden.setBackgroundResource(R.drawable.bg_search_bar);
        tvStatusHidden.setTextColor(getColor(R.color.text_secondary));

        if (active) {
            tvStatusActive.setBackgroundResource(R.drawable.bg_tab_active_dark);
            tvStatusActive.setTextColor(getColor(R.color.white));
            tvPreviewStatus.setText("Hoạt động");
            tvPreviewStatus.setBackgroundResource(R.drawable.bg_status_available);
            tvPreviewStatus.setTextColor(getColor(R.color.accent));
        } else {
            tvStatusHidden.setBackgroundResource(R.drawable.bg_tab_active_dark);
            tvStatusHidden.setTextColor(getColor(R.color.white));
            tvPreviewStatus.setText("Tạm ẩn");
            tvPreviewStatus.setBackgroundResource(R.drawable.bg_status_paused);
            tvPreviewStatus.setTextColor(getColor(android.R.color.holo_orange_dark));
        }
    }

    /**
     * Xác thực dữ liệu đầu vào và tiến hành gọi API lưu dữ liệu.
     */
    private void validateAndSave() {
        String name = edtName.getText().toString().trim();
        if (name.isEmpty()) {
            edtName.setError("Tên danh mục không được để trống");
            edtName.requestFocus();
            return;
        }

        if (resId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin nhà hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        setFormEnabled(false);

        Map<String, Object> body = new HashMap<>();
        body.put("resId", resId);
        body.put("catName", name);
        body.put("status", isActive ? 1 : 0);
        String desc = edtDesc.getText().toString().trim();
        body.put("description", desc); // Luôn gửi, kể cả rỗng để xóa được mô tả cũ

        ZappyApiService api = RetrofitClient.getApiService();

        Callback<Category> callback = new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                setFormEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    String msg = isEditMode ? "Đã cập nhật danh mục: " : "Đã thêm danh mục mới: ";
                    Toast.makeText(ThemDanhMucActivity.this,
                            msg + response.body().getCatName(), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(ThemDanhMucActivity.this,
                            "Lưu thất bại (lỗi " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                setFormEnabled(true);
                Toast.makeText(ThemDanhMucActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        // Phân nhánh: gọi API cập nhật nếu đang ở chế độ sửa, ngược lại gọi API thêm mới
        if (isEditMode && editingCategoryId != null) {
            api.updateCategory(editingCategoryId, body).enqueue(callback);
        } else {
            api.createCategory(body).enqueue(callback);
        }
    }

    private void setFormEnabled(boolean enabled) {
        if (btnSave != null) {
            btnSave.setEnabled(enabled);
            btnSave.setText(enabled ? (isEditMode ? "CẬP NHẬT" : "LƯU DANH MỤC") : "Đang lưu...");
        }
        edtName.setEnabled(enabled);
        edtDesc.setEnabled(enabled);
    }
}
