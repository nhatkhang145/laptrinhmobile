package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import android.widget.ImageButton;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
public class DangKyActivity extends AppCompatActivity {

    private EditText etRestaurantName, etRestaurantUsername, etManagerName, etEmail, etAdminUsername, etAddress, etPassword, etPasswordAgain;
    private LinearLayout btnSignUp;
    private TextView tvLogin;
    private ImageButton btnTogglePassword, btnTogglePasswordAgain;
    private boolean isPasswordVisible = false;
    private boolean isPasswordAgainVisible = false;
    private TextView tvSignUpText;
    private ImageView ivSignUpArrow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangky);

        initViews();

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(DangKyActivity.this, DangNhapActivity.class));
            finish();
        });

        btnSignUp.setOnClickListener(v -> handleSignUp());
        btnTogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglePasswordVisibility(etPassword, btnTogglePassword, true);
            }
        });
        btnTogglePasswordAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglePasswordVisibility(etPasswordAgain, btnTogglePasswordAgain, false);
            }
        });
    }

    private void togglePasswordVisibility(EditText editText, ImageButton button, boolean isMainPassword) {
        boolean visible = isMainPassword ? isPasswordVisible : isPasswordAgainVisible;

        if (visible) {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            button.setImageResource(R.drawable.ic_visibility_hidden);
        } else {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            button.setImageResource(R.drawable.ic_visibility_reveal);
        }

        editText.setHint(R.string.password_hint);
        editText.setSelection(editText.getText().length());

        if (isMainPassword) {
            isPasswordVisible = !isPasswordVisible;
        } else {
            isPasswordAgainVisible = !isPasswordAgainVisible;
        }
    }

    private void initViews() {
        ivSignUpArrow = findViewById(R.id.ivSignUpArrow);
        tvSignUpText = findViewById(R.id.tvSignUpText);
        etRestaurantName = findViewById(R.id.etRestaurantName);
        etRestaurantUsername = findViewById(R.id.etRestaurantUsername);
        etManagerName = findViewById(R.id.etManagerName);
        etEmail = findViewById(R.id.etEmail);
        etAdminUsername = findViewById(R.id.etAdminUsername);
        etAddress = findViewById(R.id.etAddress);
        etPassword = findViewById(R.id.etPassword);
        etPasswordAgain = findViewById(R.id.etPasswordAgain);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnTogglePasswordAgain = findViewById(R.id.btnTogglePasswordAgain);
    }

    private void handleSignUp() {
        String resName = etRestaurantName.getText().toString().trim();
        String resDomain = etRestaurantUsername.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String username = etAdminUsername.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String passAgain = etPasswordAgain.getText().toString().trim();
        String managerName = etManagerName.getText().toString().trim();
        if (resName.isEmpty() || resDomain.isEmpty() || managerName.isEmpty()
                || address.isEmpty() || email.isEmpty() || username.isEmpty()
                || pass.isEmpty() || passAgain.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(passAgain)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        ZappyApiService api = RetrofitClient.getApiService();
        Map<String, String> data = new HashMap<>();
        data.put("email", email);
        setLoading(true);
        api.sendRegisterOtp(data).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Intent intent = new Intent(DangKyActivity.this, OTPActivity.class);
                    intent.putExtra("mode", "register");
                    intent.putExtra("email", email);
                    intent.putExtra("resName", resName);
                    intent.putExtra("resDomain", resDomain);
                    intent.putExtra("address", address);
                    intent.putExtra("username", username);
                    intent.putExtra("password", pass);
                    intent.putExtra("managerName", managerName);
                    Toast.makeText(DangKyActivity.this, "Đã gửi mã OTP", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                } else {
                    Toast.makeText(
                            DangKyActivity.this,
                            "Lỗi HTTP: " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(DangKyActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createAdminUser(Integer resId, String username, String password, String email, String managerName) {
        ZappyApiService api = RetrofitClient.getApiService();
        Map<String, Object> userData = new HashMap<>();
        userData.put("resId", resId);
        userData.put("username", username);
        userData.put("password", password);
        userData.put("role", 1); // 1 = Quan ly
        userData.put("email", email);
        userData.put("fullname", managerName);
        api.createUser(userData).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(DangKyActivity.this, "Đăng ký thành công, vui lòng đăng nhập vào nhà hàng", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(DangKyActivity.this, DangNhapActivity.class));
                    finish();
                } else {
                    Toast.makeText(DangKyActivity.this, "Lỗi tạo tài khoản quản lý", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                setLoading(false);
                Toast.makeText(DangKyActivity.this, "Lỗi mạng tạo tài khoản: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setLoading(boolean loading) {
        btnSignUp.setEnabled(!loading);
        btnSignUp.setAlpha(loading ? 0.5f : 1f);
        tvSignUpText.setText(loading
                ? "Đang gửi mã xác nhận email..."
                : getString(R.string.signup_label));
        View[] inputs = {
                etRestaurantName,
                etRestaurantUsername,
                etManagerName,
                etEmail,
                etAdminUsername,
                etAddress,
                etPassword,
                etPasswordAgain,
                btnTogglePassword,
                btnTogglePasswordAgain,
                tvLogin
        };
        ivSignUpArrow.setVisibility(loading ? View.GONE : View.VISIBLE);
        for (View v : inputs) {
            v.setEnabled(!loading);
            v.setAlpha(loading ? 0.5f : 1f);
        }
    }
}
