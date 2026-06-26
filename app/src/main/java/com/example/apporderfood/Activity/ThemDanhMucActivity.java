package com.example.apporderfood.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

public class ThemDanhMucActivity extends AppCompatActivity {

    private EditText edtName, edtDesc;
    private TextView tvTitle, tvPreviewName, tvPreviewStatus, tvStatusActive, tvStatusHidden;
    private ImageView ivBanner, btnRemoveImage;
    private View layoutUpload, llUploadPlaceholder;
    private MaterialButton btnSave;
    private Uri selectedImageUri;
    private boolean isEditMode = false;
    private boolean isActive = true; // trạng thái mặc định
    private int resId = -1;
    private Integer editingCategoryId = null;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivBanner.setImageURI(selectedImageUri);
                    ivBanner.setVisibility(View.VISIBLE);
                    llUploadPlaceholder.setVisibility(View.GONE);
                    btnRemoveImage.setVisibility(View.VISIBLE);
                }
            }
    );

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
        ivBanner = findViewById(R.id.iv_category_banner);
        btnRemoveImage = findViewById(R.id.btn_remove_image);
        layoutUpload = findViewById(R.id.layout_upload_banner);
        llUploadPlaceholder = findViewById(R.id.ll_upload_placeholder);
        btnSave = findViewById(R.id.btn_save);
    }

    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("IS_EDIT", false)) {
            isEditMode = true;
            tvTitle.setText("Chỉnh sửa danh mục");
            if (btnSave != null) btnSave.setText("CẬP NHẬT");

            // Nhận Category object từ Intent
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

        layoutUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        btnRemoveImage.setOnClickListener(v -> {
            selectedImageUri = null;
            ivBanner.setVisibility(View.GONE);
            llUploadPlaceholder.setVisibility(View.VISIBLE);
            btnRemoveImage.setVisibility(View.GONE);
        });

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
        if (!desc.isEmpty()) body.put("description", desc);

        ZappyApiService api = RetrofitClient.getApiService();
        api.createCategory(body).enqueue(new Callback<Category>() {
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
        });
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
