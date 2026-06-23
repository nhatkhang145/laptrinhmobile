package com.example.apporderfood.Activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apporderfood.R;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.Restaurant;
import com.example.apporderfood.model.User;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DangKyActivity extends AppCompatActivity {

    private EditText etRestaurantName, etRestaurantUsername, etManagerName, etPhone, etAdminUsername, etAddress, etPassword, etPasswordAgain;
    private LinearLayout btnSignUp;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangky);

        initViews();

        tvLogin.setOnClickListener(v -> {
            // TODO: Chuyen qua Dang Nhap
            Toast.makeText(this, "Chuyển sang màn hình đăng nhập...", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(DangKyActivity.this, DangNhapActivity.class));
            // finish();
        });

        btnSignUp.setOnClickListener(v -> handleSignUp());
    }

    private void initViews() {
        etRestaurantName = findViewById(R.id.etRestaurantName);
        etRestaurantUsername = findViewById(R.id.etRestaurantUsername);
        etManagerName = findViewById(R.id.etManagerName);
        etPhone = findViewById(R.id.etPhone);
        etAdminUsername = findViewById(R.id.etAdminUsername);
        etAddress = findViewById(R.id.etAddress);
        etPassword = findViewById(R.id.etPassword);
        etPasswordAgain = findViewById(R.id.etPasswordAgain);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void handleSignUp() {
        String resName = etRestaurantName.getText().toString().trim();
        String resDomain = etRestaurantUsername.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        
        String username = etAdminUsername.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String passAgain = etPasswordAgain.getText().toString().trim();

        if (resName.isEmpty() || resDomain.isEmpty() || username.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(passAgain)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        ZappyApiService api = RetrofitClient.getApiService();

        // 1. Tao Nha hang truoc
        Map<String, String> resData = new HashMap<>();
        resData.put("resName", resName);
        resData.put("resDomain", resDomain);
        resData.put("address", address);

        btnSignUp.setEnabled(false); // disable chong click nhieu lan
        api.createRestaurant(resData).enqueue(new Callback<Restaurant>() {
            @Override
            public void onResponse(Call<Restaurant> call, Response<Restaurant> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Integer resId = response.body().getId();
                    // 2. Tao Admin User sau khi co ID nha hang
                    createAdminUser(resId, username, pass);
                } else {
                    btnSignUp.setEnabled(true);
                    Toast.makeText(DangKyActivity.this, "Domain đã tồn tại hoặc lỗi server!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Restaurant> call, Throwable t) {
                btnSignUp.setEnabled(true);
                Toast.makeText(DangKyActivity.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createAdminUser(Integer resId, String username, String password) {
        ZappyApiService api = RetrofitClient.getApiService();
        Map<String, Object> userData = new HashMap<>();
        userData.put("resId", resId);
        userData.put("username", username);
        userData.put("password", password);
        userData.put("role", 1); // 1 = Quan ly

        api.createUser(userData).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                btnSignUp.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(DangKyActivity.this, "Đăng ký thành công! Hãy đăng nhập.", Toast.LENGTH_LONG).show();
                    // TODO: Chuyen qua man hinh dang nhap hoac chinh
                } else {
                    Toast.makeText(DangKyActivity.this, "Lỗi tạo tài khoản quản lý", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                btnSignUp.setEnabled(true);
                Toast.makeText(DangKyActivity.this, "Lỗi mạng tạo tài khoản: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
