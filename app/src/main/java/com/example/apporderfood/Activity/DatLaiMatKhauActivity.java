package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apporderfood.R;
import com.example.apporderfood.api.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DatLaiMatKhauActivity extends AppCompatActivity {

    private EditText etPassword, etConfirmPassword;
    private ImageButton btnTogglePassword, btnToggleConfirmPassword;
    private LinearLayout btnResetPassword, btnBackLogin;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datlaimatkhau);
        email = getIntent().getStringExtra("email");
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBackLogin = findViewById(R.id.btnBackLogin);
        btnBackLogin.setOnClickListener(v -> {
            Intent intent = new Intent(DatLaiMatKhauActivity.this, DangNhapActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        btnTogglePassword.setOnClickListener(v -> togglePassword());
        btnToggleConfirmPassword.setOnClickListener(v -> toggleConfirmPassword());
        // Nút reset password được đặt sự kiện, gọi phương thức reset password
        btnResetPassword.setOnClickListener(v -> resetPassword());
    }
    // Phương thức phụ trách việc reset password.
    private void resetPassword() {
        // Mật khẩu mới người dùng muốn sử dụng và mật khẩu nhập lại được lấy ra
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        // Mật khẩu được kiểm tra lần lượt xem nó có rỗng không, có khác với mật khẩu nhập lại không,
        // chiều dài có bé hơn 6 không và hiển thị thông báo lỗi tương ứng.
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Thiếu email, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }
        // Sau đó email và mật khẩu mới được đặt vào một HashMap, sẵn sàng gửi đi cho API.
        Map<String, String> data = new HashMap<>();
        data.put("email", email);
        data.put("newPassword", password);
        // Nút reset password bị chặn lại trong suốt quá trình đổi mật khẩu.
        btnResetPassword.setEnabled(false);
        RetrofitClient.getApiService().resetPassword(data).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                // Dù response success hay không, nút reset password được enable trở lại.
                btnResetPassword.setEnabled(true);
                // Nếu success, hiển thị thông báo đổi mật khẩu thành công, tạo intent mới đến màn hình đan nhập.
                if (response.isSuccessful()) {
                    Toast.makeText(DatLaiMatKhauActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(DatLaiMatKhauActivity.this, DangNhapActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    // Nếu không, hiển thị thông báo đổi mật khẩu thất bại.
                } else {
                    Toast.makeText(DatLaiMatKhauActivity.this, "Đổi mật khẩu thất bại", Toast.LENGTH_SHORT).show();
                }
            }
            // Nếu thất bại, hiển thị thông báo lỗi mạng.
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                btnResetPassword.setEnabled(true);
                Toast.makeText(DatLaiMatKhauActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void togglePassword() {
        if (isPasswordVisible) {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_hidden);
            isPasswordVisible = false;
        } else {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_reveal);
            isPasswordVisible = true;
        }

        etPassword.setSelection(etPassword.getText().length());
    }

    private void toggleConfirmPassword() {
        if (isConfirmPasswordVisible) {
            etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            btnToggleConfirmPassword.setImageResource(R.drawable.ic_visibility_hidden);
            isConfirmPasswordVisible = false;
        } else {
            etConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            btnToggleConfirmPassword.setImageResource(R.drawable.ic_visibility_reveal);
            isConfirmPasswordVisible = true;
        }

        etConfirmPassword.setSelection(etConfirmPassword.getText().length());
    }
}