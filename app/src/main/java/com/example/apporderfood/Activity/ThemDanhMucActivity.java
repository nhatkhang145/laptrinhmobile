package com.example.apporderfood.Activity;

import android.content.Intent;
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

public class ThemDanhMucActivity extends AppCompatActivity {

    private EditText edtName, edtDesc;
    private TextView tvTitle, tvPreviewName, tvPreviewStatus, tvStatusActive, tvStatusHidden;
    private ImageView ivBanner, btnRemoveImage;
    private View layoutUpload, llUploadPlaceholder;
    private Uri selectedImageUri;
    private boolean isEditMode = false;

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
    }

    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("IS_EDIT", false)) {
            isEditMode = true;
            tvTitle.setText("Chỉnh sửa danh mục");
            String name = intent.getStringExtra("CATEGORY_NAME");
            String desc = intent.getStringExtra("CATEGORY_DESC");
            
            edtName.setText(name);
            edtDesc.setText(desc);
            tvPreviewName.setText(name);
            // Giả lập trạng thái cũ là Hoạt động
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

        findViewById(R.id.btn_save).setOnClickListener(v -> validateAndSave());
    }

    private void updateStatusUI(boolean isActive) {
        // Reset styles
        tvStatusActive.setBackgroundResource(R.drawable.bg_search_bar);
        tvStatusActive.setTextColor(getColor(R.color.text_secondary));
        tvStatusHidden.setBackgroundResource(R.drawable.bg_search_bar);
        tvStatusHidden.setTextColor(getColor(R.color.text_secondary));

        if (isActive) {
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

        String msg = isEditMode ? "Đã cập nhật danh mục: " : "Đã thêm danh mục mới: ";
        Toast.makeText(this, msg + name + ". Chờ kết nối API server.", Toast.LENGTH_LONG).show();
        
        setResult(RESULT_OK);
        finish();
    }
}
