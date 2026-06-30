package com.zappy.controller;

import com.zappy.entity.Restaurant;
import com.zappy.entity.User;
import com.zappy.repository.RestaurantRepository;
import com.zappy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API Dang nhap & Quan ly tai khoan
 *
 * POST /api/users/login             -> Dang nhap
 * GET  /api/users/restaurant/{resId}-> Lay danh sach nhan vien cua nha hang
 * POST /api/users                   -> Quan ly tao tai khoan nhan vien
 * PUT  /api/users/{id}/password     -> Doi mat khau
 * DELETE /api/users/{id}            -> Quan ly xoa tai khoan
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RestaurantRepository restaurantRepo;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String domain   = loginData.get("domain");   
        String username = loginData.get("username");
        String password = loginData.get("password");
        // Tim nha hang theo domain, neu ko co thi cho no null
        Restaurant restaurant = restaurantRepo.findByResDomain(domain)
                .orElse(null);
        // Neu nha hang la null, tra ra loi.
        if (restaurant == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Khong tim thay nha hang voi domain: " + domain));
        }
        // Tim user theo ten cua user va id cua nha hang, neu ko co thi null
        User user = userRepo.findByUsernameAndRestaurantId(username, restaurant.getId())
                .orElse(null);
        // Kiem tra neu user null hoac mat khau cua user ko trung voi mat khau nhan vao, bao loi.
        if (user == null || !user.getPassword().equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Sai username hoac mat khau!"));
        }
        // Kiem tra tai khoan bi khoa
        if (Boolean.FALSE.equals(user.getIsActive())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Tai khoan nay da bi khoa!"));
        }
        // Neu pass tat ca dieu kien tren, set trang thai cua user thanh online
        user.setIsOnline(true);
        
        userRepo.save(user);
        // Dang nhap thanh cong, tra ve thong tin cua user
        return ResponseEntity.ok(Map.of(
                "id",         user.getId(),
                "username",   user.getUsername(),
                "role",       user.getRole(),
                "resId",      restaurant.getId(),
                "resName",    restaurant.getResName(),
                "resDomain",  restaurant.getResDomain(),
                "fullname",   user.getFullname() != null ? user.getFullname() : "",
                "email",      user.getEmail() != null ? user.getEmail() : ""
        ));
    }

    // Lay danh sach nhan vien cua 1 nha hang
    @GetMapping("/restaurant/{resId}")
    public List<User> getByRestaurant(@PathVariable Integer resId) {
        return userRepo.findByRestaurantId(resId);
    }

    // Quan ly tao tai khoan nhan vien moi
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> data) {
        Integer resId    = (Integer) data.get("resId");
        String username  = (String)  data.get("username");
        String password  = (String)  data.get("password");
        Integer role     = (Integer) data.get("role");
        String email = (String) data.get("email");
        String fullname  = (String) data.get("fullname");
        Restaurant restaurant = restaurantRepo.findById(resId)
                .orElse(null);
        if (restaurant == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Khong tim thay nha hang!"));
        }

        User user = new User();
        user.setRestaurant(restaurant);
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role != null ? role : 0);
        user.setEmail(email);
        user.setFullname(fullname);

        return ResponseEntity.status(HttpStatus.CREATED).body(userRepo.save(user));
    }

    // Doi mat khau
    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(@PathVariable Integer id,
                                            @RequestBody Map<String, String> data) {
        String oldPassword = data.get("oldPassword");
        String newPassword = data.get("newPassword");

        return userRepo.findById(id).map(user -> {
            if (!user.getPassword().equals(oldPassword)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Mat khau cu khong dung!"));
            }
            user.setPassword(newPassword);
            userRepo.save(user);
            return ResponseEntity.ok(Map.of("message", "Doi mat khau thanh cong!"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Quan ly xoa tai khoan nhan vien
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        if (!userRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Da xoa tai khoan!"));
    }
    //Cap nhat thong tin nhan vien
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        // 1. Tìm user trong DB
        User user = userRepo.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Không tìm thấy nhân viên!"));
        }

        // 2. Cập nhật các trường thông tin nếu có truyền lên
        if (data.containsKey("fullname")) {
            user.setFullname((String) data.get("fullname"));
        }
        if (data.containsKey("email")) {
            user.setEmail((String) data.get("email"));
        }
        if (data.containsKey("role")) {
            user.setRole((Integer) data.get("role"));
        }
        
        // 3. Xử lý cập nhật mật khẩu (Nếu có check vào nút Đổi MK)
        if (data.containsKey("password")) {
            String newPassword = (String) data.get("password");
            
            user.setPassword(newPassword);
        }

        // 4. Lưu lại vào DB
        User updatedUser = userRepo.save(user);
        return ResponseEntity.ok(updatedUser);
    }
    // Dang xuat (Cap nhat trang thai Offline)
    @PutMapping("/{id}/logout")
    public ResponseEntity<?> logout(@PathVariable Integer id) {
        return userRepo.findById(id).map(user -> {
            user.setIsOnline(false); // Chuyển cờ hoạt động về false
            userRepo.save(user);
            return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công!"));
        }).orElse(ResponseEntity.notFound().build());
    }
    // Khoa / Mo khoa tai khoan nhan vien
    @PutMapping("/{id}/toggle-lock")
    public ResponseEntity<?> toggleLock(@PathVariable Integer id) {
        return userRepo.findById(id).map(user -> {
            boolean currentStatus = user.getIsActive() != null ? user.getIsActive() : true;
            user.setIsActive(!currentStatus);
            userRepo.save(user);
            return ResponseEntity.ok(Map.of("isActive", user.getIsActive()));
        }).orElse(ResponseEntity.notFound().build());
    }
}
