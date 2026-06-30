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
        // Khởi động đếm thời gian ngay khi giao diện được tạo.
        startCountdown();
        // Chặn nút gửi otp lại.
        setResendEnabled(false);
        // Lấy ra email và mode từ intent.
        email = getIntent().getStringExtra("email");
        mode = getIntent().getStringExtra("mode");
        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);
        // Gọi phương thức cài đặt các ô otp.
        setupOtpInputs();
        btnVerify = findViewById(R.id.btnVerify);
        // Gán sự kiện cho nút gửi lại mã otp, gọi phương thức resendOtp()
        tvResend.setOnClickListener(v -> {
            resendOtp();
        });
        // Gán sự kiện cho nút xác thực.
        btnVerify.setOnClickListener(v -> {
                    String otp = otp1.getText().toString().trim()
                            + otp2.getText().toString().trim()
                            + otp3.getText().toString().trim()
                            + otp4.getText().toString().trim()
                            + otp5.getText().toString().trim()
                            + otp6.getText().toString().trim();
                    // Lấy ra mã otp hoàn chỉnh từ từng ô otp nhận vào, sau đó kiểm tra xem có đủ 6
                    // số otp ko, nếu ko thì báo lỗi và ko tra ra gì.
                    if (otp.length() != 6) {
                        Toast.makeText(this, "Vui lòng nhập đủ 6 số OTP", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Đưa email nhận được từ intent và otp vào một hashmap dữ liệu được dùng để gửi đi
                    // cho API sau đó.
                    Map<String, String> data = new HashMap<>();
                    data.put("email", email);
                    data.put("otp", otp);
                    Call<Map<String, String>> call;
                    // Kiểm tra mode nhận được từ intent, nếu là register thì gọi API xác thực mã OTP của
                    // đăng ký, ngược lại thì gọi API của quên mật khẩu.
                    if ("register".equals(mode)) {
                        call = RetrofitClient.getApiService().verifyRegisterOtp(data);
                    } else {
                        call = RetrofitClient.getApiService().verifyOtp(data);
                    }

                    call.enqueue(new Callback<Map<String, String>>() {
                        @Override

                        public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                            // Nếu có response và là response successfull, tiếp tục kiểm tra mode, nếu là
                            // mode register, gọi phương thức tạo nhà hàng và tạo tài khoản admin.
                            if (response.isSuccessful()) {
                                if ("register".equals(mode)) {
                                    createRestaurantThenAdmin();
                                    // Nếu mode ko phải là mode register (tức mode quên mật khẩu), tạo intent mới chuyển tới DatLaiMatKhauActivity
                                    // và gửi kèm email của người dùng đã nhập trước đó
                                } else {
                                    Intent intent = new Intent(OTPActivity.this, DatLaiMatKhauActivity.class);
                                    intent.putExtra("email", email);
                                    startActivity(intent);
                                    finish();
                                }
                                // Nếu response ko success, hiển thị lỗi.
                            } else {
                                Toast.makeText(OTPActivity.this, "OTP không đúng hoặc đã hết hạn", Toast.LENGTH_SHORT).show();
                            }
                        }
                        // Trong trường hợp thất bại, hiển thị lỗi mạng kèm chi tiết tin nhắn lỗi.
                        @Override
                        public void onFailure(Call<Map<String, String>> call, Throwable t) {
                            Toast.makeText(OTPActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
        );
    }
    //Phương thức tạo nhà hàng và tài khoản admin.
    private void createRestaurantThenAdmin() {
        // Lần lượt lấy ra các dữ liệu gửi kèm cùng intent để tạo nhà hàng và tài khoản admin.
        String resName = getIntent().getStringExtra("resName");
        String resDomain = getIntent().getStringExtra("resDomain");
        String address = getIntent().getStringExtra("address");
        String username = getIntent().getStringExtra("username");
        String password = getIntent().getStringExtra("password");
        String managerName = getIntent().getStringExtra("managerName");
        // Các dữ liệu dùng để tạo nhà hàng được đặt vào một HashMAp riêng sau đó sẽ được gửi cho API.
        Map<String, String> resData = new HashMap<>();
        resData.put("resName", resName);
        resData.put("resDomain", resDomain);
        resData.put("address", address);
        // API tạo nhà hàng được gọi.
        RetrofitClient.getApiService().createRestaurant(resData).enqueue(new Callback<Restaurant>() {
            @Override
            public void onResponse(Call<Restaurant> call, Response<Restaurant> response) {
                // Nếu có response và response success, body của response khác null thì sau đó phương thức
                // sẽ tiếp tục tạo tài khoản admin.
                if (response.isSuccessful() && response.body() != null) {
                    // Id của nhà hàng được lấy ra, sau đó những dữ liệu cần để tạo tài khoản admin được
                    // đưa vào một HashMap, thứ sẽ được gửi đi cho API tạo tài khoản user sau đó.
                    Integer resId = response.body().getId();

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("resId", resId);
                    userData.put("username", username);
                    userData.put("password", password);
                    userData.put("role", 1);
                    userData.put("email", email);
                    userData.put("fullname", managerName);
                    // API được gọi để tạo user.
                    RetrofitClient.getApiService().createUser(userData).enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            // Nếu có response và response success, hiển thị thông báo đăng ký thành công,
                            // đồng thời chuyển sang màn hình đăng nhập và chấm dứt màn hình xác thực OTP này
                            if (response.isSuccessful()) {
                                Toast.makeText(OTPActivity.this, "Đăng ký thành công", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(OTPActivity.this, DangNhapActivity.class));
                                finish();
                                // Nếu response không success, hiển thị thông báo lỗi.
                            } else {
                                Toast.makeText(OTPActivity.this, "Lỗi tạo tài khoản quản lý", Toast.LENGTH_SHORT).show();
                            }
                        }
                        // Trong trường hợp thất bại, hiển thị lỗi mạng.
                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            Toast.makeText(OTPActivity.this, "Lỗi mạng tạo tài khoản: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    // Nếu tạo nhà hàng không thành công, hiển thị lỗi domain đã tồn tại hoặc lỗi Server.
                } else {
                    Toast.makeText(OTPActivity.this, "Domain đã tồn tại hoặc lỗi server", Toast.LENGTH_SHORT).show();
                }
            }
            // Nếu ko có response ở tạo nhà hàng, hiển thị lỗi mạng.
            @Override
            public void onFailure(Call<Restaurant> call, Throwable t) {
                Toast.makeText(OTPActivity.this, "Lỗi mạng tạo nhà hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Phương thức thực hiện việc gửi lại mã OTP.
    private void resendOtp() {
        //Nếu bộ đếm đang chạy, ngay lập tức hủy nó
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        //Chặn nút gửi otp lại để tránh việc ngươ dùng spam
        setResendEnabled(false);
        //Đặt thời gian tồn tại của OTP trên giao diện về rỗng.
        tvCountdown.setText("");
        //Đặt nội dung của nút gửi OTP lại thành "Đang gửi lại mã..."
        tvResend.setText("Đang gửi lại mã...");
        // Đưa email vào một HashMap dùng để gửi cho API sau đó.
        Map<String, String> data = new HashMap<>();
        data.put("email", email);
        Call<Map<String, String>> call;
        // Kiểm tra chế độ, nếu là register thì gọi phương thức gửi OTP tương ứng của register và ngược lại
        if ("register".equals(mode)) {
            call = RetrofitClient.getApiService().sendRegisterOtp(data);
        } else {
            call = RetrofitClient.getApiService().sendOtp(data);
        }

        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                //Nếu có response và response success, hiển thị thông báo đã gửi lại mã OTP, clear tất cả
                //số OTP đang có trong các ô nhập vào, đặt nội dung của nút gửi lại OTP về như cũ và chạy lại
                // Countdown.
                if (response.isSuccessful()) {
                    Toast.makeText(OTPActivity.this, "Đã gửi lại mã OTP", Toast.LENGTH_SHORT).show();
                    clearOtpInputs();
                    tvResend.setText("Gửi lại");
                    startCountdown();
                    // Nếu không thì hiển thị lỗi, và vẫn đặt nội dung của nút gửi lại OTP như cũ, và cho phép nút resend
                    // có thể nhấn được.
                } else {
                    Toast.makeText(OTPActivity.this, "Không thể gửi lại OTP", Toast.LENGTH_SHORT).show();
                    tvResend.setText("Gửi lại");
                    setResendEnabled(true);
                }
            }
            // Trong trường hợp thất bại, hiển thị thông báo lỗi mạng, đặt nội dung của nút gửi lại trở lại như cũ
            // và enable nút gửi lại OTP.
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(OTPActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                tvResend.setText("Gửi lại");
                setResendEnabled(true);
            }
        });
    }
    // Phương thức giúp reset toàn bộ các ô otp và đặt con trỏ về ô otp đầu tin
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
