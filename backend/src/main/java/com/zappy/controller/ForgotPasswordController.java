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
	@PostMapping("/send-otp")
	public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body) {
		String email = body.get("email");
		User user = userRepo.findByEmail(email);
		if (user == null) {
			return ResponseEntity.status(404).body(Map.of("message", "Email không tồn tại"));
		}
		String otp = String.valueOf(new Random().nextInt(900000) + 100000);
		long expireTime = System.currentTimeMillis() + OTP_EXPIRE_TIME;
		otpStorage.put(email, new OTPData(otp, expireTime));
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setSubject("Mã OTP đặt lại mật khẩu");
		message.setText("Mã OTP của bạn là: " + otp);
		mailSender.send(message);
		return ResponseEntity.ok(Map.of("message", "Đã gửi OTP"));
	}

	@PostMapping("/verify-otp")
	public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
		String email = body.get("email");
		String otp = body.get("otp");
		OTPData otpData = otpStorage.get(email);
		if (otpData == null) {
			return ResponseEntity.status(400).body(Map.of("message", "OTP không tồn tại"));
		}
		if (System.currentTimeMillis() > otpData.getExpireTime()) {
			otpStorage.remove(email);
			return ResponseEntity.status(400).body(Map.of("message", "OTP đã hết hạn"));
		}
		if (!otpData.getOtp().equals(otp)) {
			return ResponseEntity.status(400).body(Map.of("message", "OTP không đúng"));
		}
		otpStorage.remove(email);
		return ResponseEntity.ok(Map.of("message", "OTP hợp lệ"));
	}

	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
		String email = body.get("email");
		String newPassword = body.get("newPassword");

		User user = userRepo.findByEmail(email);
		if (user == null) {
			return ResponseEntity.status(404).body(Map.of("message", "Email không tồn tại"));
		}
		user.setPassword(newPassword);
		userRepo.save(user);
		otpStorage.remove(email);
		return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
	}
}