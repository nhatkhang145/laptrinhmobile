package com.example.apporderfood.Activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apporderfood.R;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.User;
import com.google.android.material.button.MaterialButton;
import com.mikepenz.iconics.view.IconicsImageView;

public class ChiTietNhanVienActivity extends AppCompatActivity {

    private IconicsImageView btnBack;
    private EditText etFullName, etEmail, etUsername, etPassword;
    private FrameLayout flRoleDropdown;
    private TextView tvSelectedRole, tvRoleTag;
    private MaterialButton btnSave;
    private com.google.android.material.checkbox.MaterialCheckBox cbChangePassword;

    private User currentUser;
    private int selectedRoleId = 0;
    private ZappyApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_nhan_vien);

        apiService = RetrofitClient.getApiService();

        // Lấy object User từ Intent
        if (getIntent().hasExtra("USER_DATA")) {
            currentUser = (User) getIntent().getSerializableExtra("USER_DATA");
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy dữ liệu nhân viên!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadDataToUI();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        flRoleDropdown = findViewById(R.id.flRoleDropdown);
        tvSelectedRole = findViewById(R.id.tvSelectedRole);
        tvRoleTag = findViewById(R.id.tvRoleTag);
        btnSave = findViewById(R.id.btnSave);
        cbChangePassword = findViewById(R.id.cbChangePassword);
    }

    private void loadDataToUI() {
        // Đổ dữ liệu vào UI
        if (currentUser.getFullname() != null) etFullName.setText(currentUser.getFullname());
        if (currentUser.getEmail() != null) etEmail.setText(currentUser.getEmail());
        etUsername.setText(currentUser.getUsername());

        // Xử lý Role
        if (currentUser.getRole() != null) {
            selectedRoleId = currentUser.getRole();
            if (selectedRoleId == 1) {
                tvSelectedRole.setText("Quản lý");
                tvRoleTag.setText("QUẢN LÝ");
                tvRoleTag.setBackgroundResource(R.drawable.bg_status_badge);
            } else if (selectedRoleId == 3) {
                tvSelectedRole.setText("Thu ngân");
                tvRoleTag.setText("THU NGÂN");
            } else {
                tvSelectedRole.setText("Nhân viên");
                tvRoleTag.setText("NHÂN VIÊN");
            }
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Nút mở menu Role
        flRoleDropdown.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, flRoleDropdown);
            popupMenu.getMenu().add(0, 1, 0, "Quản lý");
            popupMenu.getMenu().add(0, 2, 1, "Thu ngân");
            popupMenu.getMenu().add(0, 0, 2, "Nhân viên");

            popupMenu.setOnMenuItemClickListener(item -> {
                selectedRoleId = item.getItemId();
                tvSelectedRole.setText(item.getTitle());
                tvRoleTag.setText(item.getTitle().toString().toUpperCase());
                return true;
            });
            popupMenu.show();
        });

        // Sự kiện Checkbox Đổi Mật Khẩu
        cbChangePassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etPassword.setEnabled(isChecked); // Bật/tắt ô nhập theo trạng thái tick

            if (isChecked) {
                // Nếu tick vào: Xóa dấu chấm giả đi để nhập MK mới
                etPassword.setText("");
                etPassword.requestFocus();
            } else {
                // Nếu bỏ tick: Khóa lại và trả về dấu chấm giả như cũ
                etPassword.setText("••••••••");
                etPassword.clearFocus();
            }
        });

        // Nút Lưu thay đổi
        btnSave.setOnClickListener(v -> {
            // TODO: Gọi API cập nhật thông tin User
            // Nút Lưu thay đổi
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            // Tạo Map dữ liệu để gửi lên API
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("fullname", fullName);
            data.put("email", email); // Đang dùng email để lưu SĐT
            data.put("role", selectedRoleId);
            data.put("resId", currentUser.getResId());
            data.put("username", currentUser.getUsername()); // Username giữ nguyên

            // Kiểm tra xem Admin có tick vào đổi mật khẩu không
            if (cbChangePassword.isChecked()) {
                String newPassword = etPassword.getText().toString().trim();
                if (newPassword.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập mật khẩu mới!", Toast.LENGTH_SHORT).show();
                    return; // Dừng lại không gọi API nếu để trống
                }
                data.put("password", newPassword);
            }

            // Khóa nút để tránh bấm nhiều lần
            btnSave.setEnabled(false);

            // Gọi API Update
            apiService.updateUser(currentUser.getId(), data).enqueue(new retrofit2.Callback<User>() {
                @Override
                public void onResponse(retrofit2.Call<User> call, retrofit2.Response<User> response) {
                    btnSave.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(ChiTietNhanVienActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        finish(); // Đóng màn hình, quay về danh sách
                    } else {
                        Toast.makeText(ChiTietNhanVienActivity.this, "Lỗi cập nhật: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<User> call, Throwable t) {
                    btnSave.setEnabled(true);
                    Toast.makeText(ChiTietNhanVienActivity.this, "Mất kết nối Server!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}