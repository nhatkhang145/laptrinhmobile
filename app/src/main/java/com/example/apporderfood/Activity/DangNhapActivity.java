package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangnhap);

        initViews();

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(DangNhapActivity.this, DangKyActivity.class));
            finish();
        });

        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng quên mật khẩu đang được phát triển!", Toast.LENGTH_SHORT).show();
        });

        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void initViews() {
        etRestaurantUsername = findViewById(R.id.etRestaurantUsername);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
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
                    
                    // TODO: Luu thong tin dang nhap vao SharedPreferences neu can
                    
                    // Chuyen sang man hinh chinh (TienIchActivity)
                    Intent intent = new Intent(DangNhapActivity.this, TienIchActivity.class);
                    // Truyen data sang man hinh tiep theo
                    intent.putExtra("USER_ID", user.getId());
                    intent.putExtra("RES_ID", user.getResId());
                    intent.putExtra("ROLE", user.getRole());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(DangNhapActivity.this, "Sai thông tin đăng nhập hoặc nhà hàng!", Toast.LENGTH_SHORT).show();
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
