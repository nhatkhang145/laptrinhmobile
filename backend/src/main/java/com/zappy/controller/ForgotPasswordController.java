package com.zappy.controller;

import com.zappy.entity.User;
import com.zappy.model.OTPData;
import com.zappy.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth/forgot-password")
public class ForgotPasswordController {
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private JavaMailSender mailSender;
	private final Map<String, OTPData> otpStorage = new ConcurrentHashMap<>();
	private static final long OTP_EXPIRE_TIME = 60 * 1000;
	 // Phương thức gửi mã otp của lớp ForgotPassword, thực hiện việc gửi mã OTP tới email chỉ định để kiểm tra sự tồn tại của email
	@PostMapping("/send-otp")
	public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body) {
    	// Phương thức lấy email ra từ trong requestbody.
		String email = body.get("email");
		 // Sau đó tìm người dùng sử dụng email đó, nếu user với email đó không tồn tại, trả ra lỗi.
		User user = userRepo.findByEmail(email);
		if (user == null) {
			return ResponseEntity.status(404).body(Map.of("message", "Email không tồn tại"));
		}
		 // Nếu pass, tạo mã otp với giá trị từ 100000 tới 900000, sau đó tạo thời gian tồn tại cho mã OTP
        // Thời gian tồn tại bằng thời gian hiện tại cộng với thời gian hết hạn xác định trước là 60 giây.
		String otp = String.valueOf(new Random().nextInt(900000) + 100000);
		long expireTime = System.currentTimeMillis() + OTP_EXPIRE_TIME;
        // Mã OTP sau đó được đặt vào trong bộ nhớ tạm.
		otpStorage.put(email, new OTPData(otp, expireTime));
		 // Sau đó mail được tạo và thực hiện gửi đến địa chỉ email của người dùng nhập vào.
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setSubject("Mã OTP đặt lại mật khẩu");
		message.setText("Mã OTP của bạn là: " + otp);
		mailSender.send(message);
		return ResponseEntity.ok(Map.of("message", "Đã gửi OTP"));
	}
    // Phương thức xác thực mã OTP.
	@PostMapping("/verify-otp")
	public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
		// Email và mã otp được lấy ra từ request body.
		String email = body.get("email");
		String otp = body.get("otp");
		// Tiếp theo phương thức lấy mã OTP ra từ kho nhớ tạm ứng với email nhận vào.
		OTPData otpData = otpStorage.get(email);
		// Nếu null thì gửi thông báo mã otp ko tồn tại.
		if (otpData == null) {
			return ResponseEntity.status(400).body(Map.of("message", "OTP không tồn tại"));
		}
		 // Nếu thời gian hiện tại vượt qua thời gian sống của OTP, remove nó và thông báo mã OTP đã hết hạn
		if (System.currentTimeMillis() > otpData.getExpireTime()) {
			otpStorage.remove(email);
			return ResponseEntity.status(400).body(Map.of("message", "OTP đã hết hạn"));
		}
        // Nếu sai mã OTP thì hiển thị thông báo mã OTP không đúng
		if (!otpData.getOtp().equals(otp)) {
			return ResponseEntity.status(400).body(Map.of("message", "OTP không đúng"));
		}
		 // Cuối cùng loại bỏ cặp email và OTP tương ứng và trả ra thông báo hợp lệ.
		otpStorage.remove(email);
		return ResponseEntity.ok(Map.of("message", "OTP hợp lệ"));
	}
	// Phương thức đặt lại mật khẩu dùng cho quên mật khẩu
	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
		// Email và password mới được lấy ra từ request body.
		String email = body.get("email");
		String newPassword = body.get("newPassword");
		// Sau đó phương thức tìm user ứng với email, nếu không có thì hiển thị lỗi email ko tồn tại
		User user = userRepo.findByEmail(email);
		if (user == null) {
			return ResponseEntity.status(404).body(Map.of("message", "Email không tồn tại"));
		}
		// Nếu pass điều kiện trên, đặt mật khẩu mới cho user, save rồi loại bỏ cặp otp và email ra khỏi kho dữ liệu tạm otp
		// Sau đó gửi thông báo đổi mật khẩu thành công.
		user.setPassword(newPassword);
		userRepo.save(user);
		otpStorage.remove(email);
		return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
	}
}