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
        // Thêm sự kiện vào nút login, chuyển qua màn hình login và tắt màn hình đăng ký
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(DangKyActivity.this, DangNhapActivity.class));
            finish();
        });
        // Thêm sự kiện vào nút đăng ký, thực hiện phương thức handleSignUp() - thực hiện đăng ký.
        btnSignUp.setOnClickListener(v -> handleSignUp());

        // Lần lượt thêm sự kiện cho 2 nút ẩn và hiện mật khẩu, chạy phương thức tooglePasswordVisibility thực hiện việc ẩn và hiện mật khẩu
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

    //Phương thức giúp thay đổi trạng thái của nút ẩn/hiện mật khẩu.
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

    //Phương thức thực hiện đăng ký tài khoản
    private void handleSignUp() {
        // Phương thức bắt đầu bằng việc lấy lần lượt các thông tin người dùng nhập vào, trim() để loại bỏ các khoảng trắng ko cần thiê
        String resName = etRestaurantName.getText().toString().trim();
        String resDomain = etRestaurantUsername.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String username = etAdminUsername.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String passAgain = etPasswordAgain.getText().toString().trim();
        String managerName = etManagerName.getText().toString().trim();
        // Phương thức kiểm tra xem có thiếu thông tin nào ko, nếu có thì thông báo lỗi và ko trả ra gì.
        if (resName.isEmpty() || resDomain.isEmpty() || managerName.isEmpty()
                || address.isEmpty() || email.isEmpty() || username.isEmpty()
                || pass.isEmpty() || passAgain.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        // Phương thức kiểm tra mật khẩu nhập vào và mật khẩu nhập lại, nếu ko khớp thì báo lỗi và ko trả ra gì.
        if (!pass.equals(passAgain)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }
        // Phương thức gọi api, gửi otp đăng ký với dữ liệu đặt vào là email của người dùng vừa nhập.
        ZappyApiService api = RetrofitClient.getApiService();
        Map<String, String> data = new HashMap<>();
        data.put("email", email);
        // Trước khi gọi phương thức của API, phương thức setLoading() được gọi, khóa tất cả những
        // ô nhập và nút xác nhận đăng ký để tránh người dùng spam gây lỗi ko đáng có, đồng thời đổi nội
        // dung của nút thành "Đang gửi mã xác nhận email..." để người dùng biết.
        setLoading(true);
        // Phương thức gửi mã otp của api được gọi.
        api.sendRegisterOtp(data).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                // Nếu có response, dù success hay fail thì setLoading cũng cho thành false để ngừng
                // việc vô hiệu hóa các edit text và nút xác nhận đăng ký.
                setLoading(false);
                // Nếu response success, tạo một intent mới chỉ tới OTPActivity, đồng thời gửi các thông
                // tin cần thiết để tạo tài khoản sang đó, và gửi thông báo rằng đã gửi mã OTP.
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
                    // Trong trường hợp khác ko success, hiển thị mã lỗi.
                    Toast.makeText(
                            DangKyActivity.this,
                            "Lỗi HTTP: " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
            // Trong trường hợp ko có response, cũng setLoading thành false và hiển thị lỗi.
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
