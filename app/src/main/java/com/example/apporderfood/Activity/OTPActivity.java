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
import com.example.apporderfood.model.Restaurant;
import com.example.apporderfood.model.User;

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
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xacthuc);
        tvCountdown = findViewById(R.id.tvCountdown);
        tvResend = findViewById(R.id.tvResend);
        startCountdown();
        setResendEnabled(false);
        email = getIntent().getStringExtra("email");
        mode = getIntent().getStringExtra("mode");
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
                    Call<Map<String, String>> call;

                    if ("register".equals(mode)) {
                        call = RetrofitClient.getApiService().verifyRegisterOtp(data);
                    } else {
                        call = RetrofitClient.getApiService().verifyOtp(data);
                    }

                    call.enqueue(new Callback<Map<String, String>>() {
                        @Override
                        public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                            if (response.isSuccessful()) {
                                if ("register".equals(mode)) {
                                    createRestaurantThenAdmin();
                                } else {
                                    Intent intent = new Intent(OTPActivity.this, DatLaiMatKhauActivity.class);
                                    intent.putExtra("email", email);
                                    startActivity(intent);
                                    finish();
                                }
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

    private void createRestaurantThenAdmin() {
        String resName = getIntent().getStringExtra("resName");
        String resDomain = getIntent().getStringExtra("resDomain");
        String address = getIntent().getStringExtra("address");
        String username = getIntent().getStringExtra("username");
        String password = getIntent().getStringExtra("password");
        String managerName = getIntent().getStringExtra("managerName");

        Map<String, String> resData = new HashMap<>();
        resData.put("resName", resName);
        resData.put("resDomain", resDomain);
        resData.put("address", address);

        RetrofitClient.getApiService().createRestaurant(resData).enqueue(new Callback<Restaurant>() {
            @Override
            public void onResponse(Call<Restaurant> call, Response<Restaurant> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Integer resId = response.body().getId();

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("resId", resId);
                    userData.put("username", username);
                    userData.put("password", password);
                    userData.put("role", 1);
                    userData.put("email", email);
                    userData.put("fullname", managerName);

                    RetrofitClient.getApiService().createUser(userData).enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(OTPActivity.this, "Đăng ký thành công", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(OTPActivity.this, DangNhapActivity.class));
                                finish();
                            } else {
                                Toast.makeText(OTPActivity.this, "Lỗi tạo tài khoản quản lý", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            Toast.makeText(OTPActivity.this, "Lỗi mạng tạo tài khoản: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(OTPActivity.this, "Domain đã tồn tại hoặc lỗi server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Restaurant> call, Throwable t) {
                Toast.makeText(OTPActivity.this, "Lỗi mạng tạo nhà hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendOtp() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        setResendEnabled(false);
        tvCountdown.setText("");
        tvResend.setText("Đang gửi lại mã...");
        Map<String, String> data = new HashMap<>();
        data.put("email", email);
        Call<Map<String, String>> call;
        if ("register".equals(mode)) {
            call = RetrofitClient.getApiService().sendRegisterOtp(data);
        } else {
            call = RetrofitClient.getApiService().sendOtp(data);
        }
        call.enqueue(new Callback<Map<String, String>>() {
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
