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
        btnSendCode.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }
            rightArrowSendCode.setVisibility(View.GONE);
            tvSendCode.setText("Vui lòng chờ gửi mã...");
            etEmail.setEnabled(false);
            etEmail.setAlpha(0.5f);
            btnSendCode.setEnabled(false);
            btnSendCode.setAlpha(0.5f);
            Map<String, String> data = new HashMap<>();
            data.put("email", email);
            RetrofitClient.getApiService().sendOtp(data).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(QuenMatKhauActivity.this, "Đã gửi mã OTP", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(QuenMatKhauActivity.this, OTPActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(QuenMatKhauActivity.this, "Email không tồn tại", Toast.LENGTH_SHORT).show();
                        rightArrowSendCode.setVisibility(View.VISIBLE);
                        tvSendCode.setText("Gửi mã xác nhận");
                        etEmail.setEnabled(true);
                        etEmail.setAlpha(1f);
                        btnSendCode.setEnabled(true);
                        btnSendCode.setAlpha(1f);
                    }
                }
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
