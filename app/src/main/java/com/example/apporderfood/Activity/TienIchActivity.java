package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.content.SharedPreferences;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.apporderfood.R;
import com.example.apporderfood.api.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.apporderfood.api.RetrofitClient;
import java.util.Map;

public class TienIchActivity extends AppCompatActivity {

    private LinearLayout menuChangePassword;
    private LinearLayout menuLogout;
    private LinearLayout menuQuanLyMonAn;
    private LinearLayout menuQuanLyBan;
    private LinearLayout menuQuanLyDanhMuc;
    private LinearLayout menuQuanLyHoaDon;
    private LinearLayout menuQuanLyCaLam;
    private LinearLayout navOrder;
    private LinearLayout navSoDo;
    private LinearLayout navTienIch;
    private TextView tvUserName;
    private LinearLayout menuQuanLyNhanVien,menuThongKe;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tienich);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        initViews();
        setupClickListeners();
        animateEntrance();
        loadUserInfo();
    }

    private void initViews() {
        menuChangePassword = findViewById(R.id.menuChangePassword);
        menuLogout         = findViewById(R.id.menuLogout);
        menuQuanLyMonAn    = findViewById(R.id.menuQuanLyMonAn);
        menuQuanLyBan      = findViewById(R.id.menuQuanLyBan);
        menuQuanLyDanhMuc  = findViewById(R.id.menuQuanLyDanhMuc);
        menuQuanLyHoaDon   = findViewById(R.id.menuQuanLyHoaDon);
        menuQuanLyCaLam    = findViewById(R.id.menuQuanLyCaLam);
        navOrder           = findViewById(R.id.navOrder);
        navSoDo            = findViewById(R.id.navSoDo);
        navTienIch         = findViewById(R.id.navTienIch);
        tvUserName         = findViewById(R.id.tvUserName);
        menuQuanLyNhanVien = findViewById(R.id.menuQuanLyNhanVien);
        menuThongKe= findViewById(R.id.menuThongKe);
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        String fullname = prefs.getString("FULLNAME", "");
        int role = prefs.getInt("ROLE", 0);
        
        if (fullname != null && !fullname.trim().isEmpty()) {
            tvUserName.setText(fullname);
        } else {
            tvUserName.setText("Chưa cập nhật tên");
        }

        if (role == 0) { // Nhan vien
            View tvAdminSectionTitle = findViewById(R.id.tvAdminSectionTitle);
            View adminMenuCard = findViewById(R.id.adminMenuCard);
            if (tvAdminSectionTitle != null) tvAdminSectionTitle.setVisibility(View.GONE);
            if (adminMenuCard != null) adminMenuCard.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {

        findViewById(R.id.btnClose).setOnClickListener(v -> {
            animatePress(v);
            finish();
        });

        menuQuanLyMonAn.setOnClickListener(v -> {
            animatePress(v);
            startActivity(new Intent(this, FoodManageActivity.class));
        });

        menuQuanLyBan.setOnClickListener(v -> {
            animatePress(v);
            startActivity(new Intent(this, TableManageActivity.class));
        });

        menuQuanLyDanhMuc.setOnClickListener(v -> {
            animatePress(v);
            startActivity(new Intent(this, CategoryManageActivity.class));
        });

        if (menuQuanLyHoaDon != null) {
            menuQuanLyHoaDon.setOnClickListener(v -> {
                animatePress(v);
                startActivity(new Intent(this, InvoiceManageActivity.class));
            });
        }
        
        if (menuQuanLyCaLam != null) {
            menuQuanLyCaLam.setOnClickListener(v -> {
                animatePress(v);
                startActivity(new Intent(this, QuanLyCaActivity.class));
            });
        }

        menuChangePassword.setOnClickListener(v -> {
            animatePress(v);
            startActivity(new Intent(this, ChangePasswordActivity.class));
        });

        menuLogout.setOnClickListener(v -> {
            animatePress(v);
            showLogoutDialog();
        });

        navOrder.setOnClickListener(v -> {
            startActivity(new Intent(this, DanhSachOrderActivity.class));
            overridePendingTransition(0, 0);
        });

        navSoDo.setOnClickListener(v -> {
            startActivity(new Intent(this, SoDobanActivity.class));
            overridePendingTransition(0, 0);
        });

        navTienIch.setOnClickListener(v -> { });

        menuQuanLyNhanVien.setOnClickListener(v -> {
            animatePress(v);
            startActivity(new Intent(this, QuanLyNhanVienActivity.class));
        });
        menuThongKe.setOnClickListener(v ->{
            animatePress(v);
            startActivity(new Intent(this, ThongKeActivity.class));
        });
    }

    private void animateEntrance() {
        View profileCard  = findViewById(R.id.profileCard);
        View sectionTitle = findViewById(R.id.tvSectionTitle);
        View menuCard     = findViewById(R.id.menuCard);

        profileCard.setAlpha(0f);
        profileCard.setTranslationY(40f);
        sectionTitle.setAlpha(0f);
        menuCard.setAlpha(0f);
        menuCard.setTranslationY(30f);

        profileCard.animate()
                .alpha(1f).translationY(0f)
                .setDuration(420)
                .setInterpolator(new DecelerateInterpolator())
                .setStartDelay(100).start();

        sectionTitle.animate()
                .alpha(1f)
                .setDuration(350)
                .setStartDelay(280).start();

        menuCard.animate()
                .alpha(1f).translationY(0f)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator())
                .setStartDelay(350).start();
    }

    private void animatePress(View view) {
        view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80)
                .withEndAction(() ->
                        view.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                .start();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất khỏi tài khoản không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    updateOfflineStatus();
                    Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                    getSharedPreferences("ZappySession", MODE_PRIVATE).edit().clear().apply();
                    Intent intent = new Intent(TienIchActivity.this, DangNhapActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void updateOfflineStatus() {

        int userId = getSharedPreferences("ZappySession", MODE_PRIVATE).getInt("USER_ID", -1);
        if (userId == -1) {
            return;
        }
        RetrofitClient.getApiService().logoutUser(userId).enqueue(new Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(Call<Map<String, String>> call,Response<Map<String, String>> response) {}
                    @Override
                    public void onFailure(Call<Map<String, String>> call,Throwable t) {}
                });
    }
}
