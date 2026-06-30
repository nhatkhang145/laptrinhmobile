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

    //Phương thức thực hiện đăng nhập.
    private void handleLogin() {
        //Lấy ra dữ liệu từ 3 cái edit text gồm domain (tên nhà hàng), username (tên đăng nhập của người dùng), password (mật khẩu của người dùng)
        String domain = etRestaurantUsername.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        //Kiểm tra từng cái dữ liệu trên, một trong số nó rỗng thì gọi Toast nhảy thông báo và ko trả ra gì.
        if (domain.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        //Khóa nút đăng nhập lại vào lúc người dùng nhấn để tránh spam gây lỗi ko cần thiết.
        btnLogin.setEnabled(false);
        //Gọi retrofitclient lấy api để xử lí tiếp.
        ZappyApiService api = RetrofitClient.getApiService();
        //Đưa dữ liệu vào một cái hash map để gửi đi cho backend.
        Map<String, String> loginData = new HashMap<>();
        loginData.put("domain", domain);
        loginData.put("username", username);
        loginData.put("password", password);
        //Kêu api gọi phương thức login bất đồng bộ (enqueue ấy) với đầu vào là login data,
        api.login(loginData).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                //Nếu có trả về (đang nhập được) thì nút đăng nhập được enable trở lại.
                btnLogin.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    //Lấy đối tượng user
                    User user = response.body();
                    //Hiện thông báo đăng nhập thành công
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
                    // Xóa màn hình đăng nhập.
                    finish();

                } else {
                    //Tạo string thông báo lỗi để phòng ko đọc được response.
                    String errorMsg = "Sai thông tin đăng nhập hoặc nhà hàng!";
                    try {
                        // Nếu body của response ko rỗng, lấy
                        if (response.errorBody() != null) {
                            String errBody = response.errorBody().string();
                            //Chuyển json thành dạng object và lấy giá trị message.
                            org.json.JSONObject jObjError = new org.json.JSONObject(errBody);
                            errorMsg = jObjError.getString("message");
                        }
                    } catch (Exception e) {
                        errorMsg += " (HTTP " + response.code() + ")";
                    }
                    //Trong trường hợp ko đọc được response thì sài cái lỗi tạo sẵn thông báo lên.
                    Toast.makeText(DangNhapActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }
            // Nếu ko nhận được response, enable nút đăng nhập lại, và hiển thị lỗi mạng.
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                btnLogin.setEnabled(true);
                Toast.makeText(DangNhapActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
