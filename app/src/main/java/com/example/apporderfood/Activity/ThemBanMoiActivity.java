package com.example.apporderfood.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.example.apporderfood.R;
import com.google.android.material.button.MaterialButton;

public class ThemBanMoiActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_ban_moi);

        btnBack = findViewById(R.id.btn_back);
        btnConfirm = findViewById(R.id.btn_confirm);

        btnBack.setOnClickListener(v -> finish());

        btnConfirm.setOnClickListener(v -> {
            // Xử lý thêm bàn mới ở đây
            finish();
        });
    }
}
