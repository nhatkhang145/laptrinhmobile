package com.example.apporderfood.Activity;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.apporderfood.R;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;
import com.mikepenz.iconics.view.IconicsImageView;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;

    private IconicsImageView ivToggleCurrent;
    private IconicsImageView ivToggleNew;
    private IconicsImageView ivToggleConfirm;

    private boolean currentVisible = false;
    private boolean newVisible     = false;
    private boolean confirmVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doimatkhau);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.changePasswordRoot), (v, insets) -> {
                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(bars.left, bars.top, bars.right, 0);
                    return insets;
                });

        initViews();
        setupToggleListeners();
        setupButtonListeners();
        animateEntrance();
    }

    private void initViews() {
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword     = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        ivToggleCurrent   = findViewById(R.id.ivToggleCurrent);
        ivToggleNew       = findViewById(R.id.ivToggleNew);
        ivToggleConfirm   = findViewById(R.id.ivToggleConfirm);
    }

    private void setupToggleListeners() {
        ivToggleCurrent.setOnClickListener(v -> {
            currentVisible = !currentVisible;
            togglePasswordVisibility(etCurrentPassword, ivToggleCurrent, currentVisible);
        });
        ivToggleNew.setOnClickListener(v -> {
            newVisible = !newVisible;
            togglePasswordVisibility(etNewPassword, ivToggleNew, newVisible);
        });
        ivToggleConfirm.setOnClickListener(v -> {
            confirmVisible = !confirmVisible;
            togglePasswordVisibility(etConfirmPassword, ivToggleConfirm, confirmVisible);
        });
    }


    private void togglePasswordVisibility(EditText editText,
                                          IconicsImageView iconView,
                                          boolean visible) {
        int cursorPos = editText.getSelectionEnd();

        if (visible) {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            iconView.setImageDrawable(
                    new IconicsDrawable(this, FontAwesome.Icon.faw_eye));
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            iconView.setImageDrawable(
                    new IconicsDrawable(this, FontAwesome.Icon.faw_eye_slash));
        }

        editText.setSelection(Math.min(cursorPos, editText.getText().length()));


        iconView.animate().scaleX(0.8f).scaleY(0.8f).setDuration(80)
                .withEndAction(() ->
                        iconView.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                .start();
    }

    private void setupButtonListeners() {

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            animatePress(v);
            finish();
        });


        Button btnUpdate = findViewById(R.id.btnUpdatePassword);
        btnUpdate.setOnClickListener(v -> {
            animatePress(v);
            validateAndSubmit();
        });
    }

    private void validateAndSubmit() {
        String current = etCurrentPassword.getText().toString().trim();
        String newPass  = etNewPassword.getText().toString().trim();
        String confirm  = etConfirmPassword.getText().toString().trim();

        if (current.isEmpty()) {
            etCurrentPassword.setError("Vui lòng nhập mật khẩu hiện tại");
            etCurrentPassword.requestFocus();
            return;
        }
        if (newPass.isEmpty()) {
            etNewPassword.setError("Vui lòng nhập mật khẩu mới");
            etNewPassword.requestFocus();
            return;
        }
        if (newPass.length() < 8) {
            etNewPassword.setError("Mật khẩu phải ít nhất 8 kí tự");
            etNewPassword.requestFocus();
            return;
        }
        if (!newPass.equals(confirm)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            etConfirmPassword.requestFocus();
            return;
        }

        Toast.makeText(this, "Cập nhật mật khẩu thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void animateEntrance() {
        View title    = findViewById(R.id.tvPageTitle);
        View subtitle = findViewById(R.id.tvPageSubtitle);

        title.setAlpha(0f);
        title.setTranslationY(30f);
        subtitle.setAlpha(0f);

        title.animate()
                .alpha(1f).translationY(0f)
                .setDuration(380)
                .setInterpolator(new DecelerateInterpolator())
                .setStartDelay(80).start();

        subtitle.animate()
                .alpha(1f)
                .setDuration(350)
                .setStartDelay(200).start();
    }

    private void animatePress(View v) {
        v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80)
                .withEndAction(() ->
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                .start();
    }
}
