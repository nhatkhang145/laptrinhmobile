package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apporderfood.R;
import com.example.apporderfood.api.RetrofitClient;

import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class OTPActivity extends AppCompatActivity {

    private EditText otp1, otp2, otp3, otp4, otp5, otp6;
    private LinearLayout btnVerify;
    private String email;
    private TextView tvCountdown;
    private CountDownTimer countDownTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xacthuc);
        tvCountdown = findViewById(R.id.tvCountdown);
        startCountdown();
        email = getIntent().getStringExtra("email");

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);
        btnVerify = findViewById(R.id.btnVerify);

        btnVerify.setOnClickListener(v -> {
            String otp = otp1.getText().toString().trim()
                    + otp2.getText().toString().trim()
                    + otp3.getText().toString().trim()
                    + otp4.getText().toString().trim()
                    + otp5.getText().toString().trim()
                    + otp6.getText().toString().trim();

            if (otp.length() != 6) {
                Toast.makeText(this, "Vui lòng nhập đủ 6 số OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, String> data = new HashMap<>();
            data.put("email", email);
            data.put("otp", otp);

            RetrofitClient.getApiService().verifyOtp(data).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Intent intent = new Intent(OTPActivity.this, DatLaiMatKhauActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(OTPActivity.this, "OTP không đúng hoặc đã hết hạn", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    Toast.makeText(OTPActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        );

    }

    private void startCountdown() {
        countDownTimer = new CountDownTimer(30 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsLeft = millisUntilFinished / 1000;
                tvCountdown.setText("(" + secondsLeft + "s)");
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("(hết hạn)");
                Toast.makeText(OTPActivity.this, "OTP đã hết hạn", Toast.LENGTH_SHORT).show();
            }
        };

        countDownTimer.start();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
