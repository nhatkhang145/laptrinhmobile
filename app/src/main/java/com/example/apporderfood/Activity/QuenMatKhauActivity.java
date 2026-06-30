package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apporderfood.R;
import com.example.apporderfood.api.RetrofitClient;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuenMatKhauActivity extends AppCompatActivity {

    private EditText etEmail;
    private LinearLayout btnSendCode, btnBackLogin;
    private ImageView rightArrowSendCode;
    private TextView tvSendCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quenmatkhau);
        tvSendCode = findViewById(R.id.tvSendCode);
        etEmail = findViewById(R.id.etEmail);
        rightArrowSendCode = findViewById(R.id.rightArrowSendCode);
        btnSendCode = findViewById(R.id.btnSendCode);
        btnBackLogin = findViewById(R.id.btnBackLogin);
        btnBackLogin.setOnClickListener(v -> finish());
        // Gán sự kiện cho nút gửi mã xác thực
        btnSendCode.setOnClickListener(v -> {
            // Địa chỉ email được lấy ra từ edit text mà người dùng nhập vào
            String email = etEmail.getText().toString().trim();
            // Nếu email rỗng, hiển thị thông báo vui lòng nhập email và ko trả ra gì cả
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }
            // Sau đó ẩn mũi tên trên nút gửi mã, đổi nội dung của nút thành vui lòng chờ gửi mã và thay đổi màu sắc.
            // Đồng thời chặn các edit text để ngăn người dùng nhập gì vào.
            rightArrowSendCode.setVisibility(View.GONE);
            tvSendCode.setText("Vui lòng chờ gửi mã...");
            etEmail.setEnabled(false);
            etEmail.setAlpha(0.5f);
            btnSendCode.setEnabled(false);
            btnSendCode.setAlpha(0.5f);
            // Sau đó địa chỉ email người dùng nhập vào được đặt vào một HashMap, thứ sẽ được gửi đi sau đó cho API.
            Map<String, String> data = new HashMap<>();
            data.put("email", email);
            RetrofitClient.getApiService().sendOtp(data).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    // Nếu có response và response success
                    if (response.isSuccessful()) {
                        // Hiển thị thông báo đã gửi mã OTP, tạo intent chuyển qua trang xác thực mã OTP kèm mode và email.
                        Toast.makeText(QuenMatKhauActivity.this, "Đã gửi mã OTP", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(QuenMatKhauActivity.this, OTPActivity.class);
                        intent.putExtra("mode", "forgot_password");
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();

                    } else {
                        // Nếu response ko success, hiển thị lỗi email ko tồn tại, đặt nội dung nút gửi mã về như cũ.
                        // Ngừng chặn và đặt màu sắc của các button, edit text về như cũ.
                        Toast.makeText(QuenMatKhauActivity.this, "Email không tồn tại", Toast.LENGTH_SHORT).show();
                        rightArrowSendCode.setVisibility(View.VISIBLE);
                        tvSendCode.setText("Gửi mã xác nhận");
                        etEmail.setEnabled(true);
                        etEmail.setAlpha(1f);
                        btnSendCode.setEnabled(true);
                        btnSendCode.setAlpha(1f);
                    }
                }
                // Nếu thất bại, hiển thị thông báo lỗi mạng, và đặt màu sắc, nội dung của các edit text, button
                // về như cũ, cũng như ngừng việc chặn.
                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    Toast.makeText(QuenMatKhauActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    rightArrowSendCode.setVisibility(View.VISIBLE);
                    tvSendCode.setText("Gửi mã xác nhận");
                    etEmail.setEnabled(true);
                    etEmail.setAlpha(1f);
                    btnSendCode.setEnabled(true);
                    btnSendCode.setAlpha(1f);
                }
            });
        });
    }
}
