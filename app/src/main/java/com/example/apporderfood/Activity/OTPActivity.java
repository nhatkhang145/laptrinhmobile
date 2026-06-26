package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;

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
    private TextView tvCountdown, tvResend;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xacthuc);
        tvCountdown = findViewById(R.id.tvCountdown);
        tvResend = findViewById(R.id.tvResend);
        startCountdown();
        setResendEnabled(false);
        email = getIntent().getStringExtra("email");

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);
        setupOtpInputs();
        btnVerify = findViewById(R.id.btnVerify);
        tvResend.setOnClickListener(v -> {
            resendOtp();
        });
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

    private void resendOtp() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        setResendEnabled(false);
        tvCountdown.setText("");
        tvResend.setText("Đang gửi...");
        Map<String, String> data = new HashMap<>();
        data.put("email", email);
        RetrofitClient.getApiService().sendOtp(data).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(OTPActivity.this, "Đã gửi lại mã OTP", Toast.LENGTH_SHORT).show();
                    clearOtpInputs();
                    tvResend.setText("Gửi lại");
                    startCountdown();
                } else {
                    Toast.makeText(OTPActivity.this, "Không thể gửi lại OTP", Toast.LENGTH_SHORT).show();
                    tvResend.setText("Gửi lại");
                    setResendEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(OTPActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                tvResend.setText("Gửi lại");
                setResendEnabled(true);
            }
        });
    }

    private void clearOtpInputs() {
        otp1.setText("");
        otp2.setText("");
        otp3.setText("");
        otp4.setText("");
        otp5.setText("");
        otp6.setText("");
        otp1.requestFocus();
    }

    private void setupOtpInputs() {
        EditText[] otpBoxes = {otp1, otp2, otp3, otp4, otp5, otp6};
        for (int i = 0; i < otpBoxes.length; i++) {
            int index = i;
            otpBoxes[index].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpBoxes.length - 1) {
                        otpBoxes[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
            otpBoxes[index].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && otpBoxes[index].getText().toString().isEmpty()
                        && index > 0) {
                    otpBoxes[index - 1].requestFocus();
                    otpBoxes[index - 1].setText("");
                    return true;
                }
                return false;
            });
        }
    }

    private void startCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        setResendEnabled(false);

        countDownTimer = new CountDownTimer(60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsLeft = millisUntilFinished / 1000;
                tvCountdown.setText("(" + secondsLeft + "s)");
                if (secondsLeft <= 40) {
                    setResendEnabled(true);
                }
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("(hết hạn)");
                Toast.makeText(OTPActivity.this, "OTP đã hết hạn", Toast.LENGTH_SHORT).show();
                setResendEnabled(true);
            }
        };
        countDownTimer.start();
    }

    private void setResendEnabled(boolean enabled) {
        tvResend.setEnabled(enabled);
        if (enabled) {
            tvResend.setAlpha(1f);
        } else {
            tvResend.setAlpha(0.5f);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
