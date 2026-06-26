package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apporderfood.R;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.User;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DangNhapActivity extends AppCompatActivity {

    private EditText etRestaurantUsername, etUsername, etPassword;
    private LinearLayout btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private ImageButton btnTogglePassword;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangnhap);

        initViews();

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(DangNhapActivity.this, DangKyActivity.class));
            finish();
        });

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(DangNhapActivity.this, QuenMatKhauActivity.class))
        );

        btnLogin.setOnClickListener(v -> handleLogin());
        btnTogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglePasswordVisibility();
            }
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setTransformationMethod(
                    PasswordTransformationMethod.getInstance());
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_hidden);
            isPasswordVisible = false;
        } else {
            etPassword.setTransformationMethod(
                    HideReturnsTransformationMethod.getInstance());
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_reveal);
            isPasswordVisible = true;
        }

        etPassword.setSelection(etPassword.getText().length());
    }

    private void initViews() {
        etRestaurantUsername = findViewById(R.id.etRestaurantUsername);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
    }

    private void handleLogin() {
        String domain = etRestaurantUsername.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (domain.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        ZappyApiService api = RetrofitClient.getApiService();

        Map<String, String> loginData = new HashMap<>();
        loginData.put("domain", domain);
        loginData.put("username", username);
        loginData.put("password", password);

        api.login(loginData).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                btnLogin.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    Toast.makeText(DangNhapActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                    // Lưu thông tin đăng nhập vào SharedPreferences để dùng toàn app
                    getSharedPreferences("ZappySession", MODE_PRIVATE)
                            .edit()
                            .putInt("USER_ID", user.getId())
                            .putInt("RES_ID", user.getResId())
                            .putInt("ROLE", user.getRole())
                            .putString("USERNAME", user.getUsername())
                            .putString("FULLNAME", user.getFullname())
                            .apply();

                    // Chuyển sang màn hình chính
                    Intent intent = new Intent(DangNhapActivity.this, TienIchActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    String errorMsg = "Sai thông tin đăng nhập hoặc nhà hàng!";
                    try {
                        if (response.errorBody() != null) {
                            String errBody = response.errorBody().string();
                            // errBody is JSON like {"message":"..."}
                            org.json.JSONObject jObjError = new org.json.JSONObject(errBody);
                            errorMsg = jObjError.getString("message");
                        }
                    } catch (Exception e) {
                        errorMsg += " (HTTP " + response.code() + ")";
                    }
                    Toast.makeText(DangNhapActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                btnLogin.setEnabled(true);
                Toast.makeText(DangNhapActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
