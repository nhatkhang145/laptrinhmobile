package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.apporderfood.R;

public class TienIchActivity extends AppCompatActivity {

    private LinearLayout menuChangePassword;
    private LinearLayout menuLogout;
    private LinearLayout menuQuanLyMonAn;
    private LinearLayout menuQuanLyBan;
    private LinearLayout menuQuanLyDanhMuc;
    private LinearLayout navOrder;
    private LinearLayout navSoDo;
    private LinearLayout navTienIch;

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
    }

    private void initViews() {
        menuChangePassword = findViewById(R.id.menuChangePassword);
        menuLogout         = findViewById(R.id.menuLogout);
        menuQuanLyMonAn    = findViewById(R.id.menuQuanLyMonAn);
        menuQuanLyBan      = findViewById(R.id.menuQuanLyBan);
        menuQuanLyDanhMuc  = findViewById(R.id.menuQuanLyDanhMuc);
        navOrder           = findViewById(R.id.navOrder);
        navSoDo            = findViewById(R.id.navSoDo);
        navTienIch         = findViewById(R.id.navTienIch);
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
                .setMessage("Ban co chac muon dang xuat khoi tai khoan khong?")
                .setPositiveButton("Dang xuat", (dialog, which) ->
                        Toast.makeText(this, "Da dang xuat", Toast.LENGTH_SHORT).show())
                .setNegativeButton("Huy", null)
                .show();
    }
}
