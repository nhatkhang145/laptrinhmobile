package com.example.apporderfood.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.apporderfood.R;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.User;
import com.google.android.material.button.MaterialButton;
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThemNhanVienActivity extends AppCompatActivity {

    private IconicsImageView btnBack;
    private EditText etFullName, etPhone, etUsername, etPassword;
    private FrameLayout flRoleDropdown;
    private TextView tvSelectedRole;
    private MaterialButton btnConfirm;

    private ZappyApiService apiService;

    private int currentResId ;
    private int selectedRoleId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_them_nhan_vien);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        currentResId = prefs.getInt("RES_ID", -1);
        if (currentResId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin nhà hàng. Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();

            // Thoát ra màn hình đăng nhập
            Intent intent = new Intent(this, DangNhapActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        initViews();
        setupListeners();

        apiService = RetrofitClient.getApiService();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);

        flRoleDropdown = findViewById(R.id.flRoleDropdown);
        tvSelectedRole = findViewById(R.id.tvSelectedRole);

        btnConfirm = findViewById(R.id.btnConfirm);

        // Gán text mặc định ban đầu
        tvSelectedRole.setText("Nhân viên");
    }

    private void setupListeners() {
        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Mở menu chọn quyền
        flRoleDropdown.setOnClickListener(v -> showRoleMenu());

        // Nút Xác nhận thêm
        btnConfirm.setOnClickListener(v -> handleAddStaff());
    }

    private void showRoleMenu() {
        PopupMenu popupMenu = new PopupMenu(this, flRoleDropdown);
        popupMenu.getMenu().add(0, 1, 0, "Quản lý");
        popupMenu.getMenu().add(0, 0, 1, "Nhân viên");
        popupMenu.getMenu().add(0, 2, 1, "Thu ngân");

        popupMenu.setOnMenuItemClickListener(item -> {
            selectedRoleId = item.getItemId(); // 1 hoặc 0
            tvSelectedRole.setText(item.getTitle());
            return true;
        });
        popupMenu.show();
    }

    private void handleAddStaff() {
        String fullName = etFullName.getText().toString().trim();
        String email = etPhone.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate cơ bản
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ Username, Password và Email!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo Map dữ liệu gửi lên API
        Map<String, Object> data = new HashMap<>();
        data.put("resId", currentResId);
        data.put("username", username);
        data.put("password", password);
        data.put("role", selectedRoleId);
        data.put("email", email);
        data.put("fullname", fullName);

        // Khóa nút để tránh bấm 2 lần
        btnConfirm.setEnabled(false);

        apiService.createUser(data).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                btnConfirm.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ThemNhanVienActivity.this, "Thêm nhân viên thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Báo cho QuanLyNhanVienActivity biết để load lại
                    finish(); // Đóng màn hình, quay về danh sách
                } else {
                    Toast.makeText(ThemNhanVienActivity.this, "Tài khoản đã tồn tại hoặc lỗi Server!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                btnConfirm.setEnabled(true);
                Toast.makeText(ThemNhanVienActivity.this, "Mất kết nối Server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}