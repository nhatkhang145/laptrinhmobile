package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.apporderfood.R;
import com.example.apporderfood.model.Area;
import com.example.apporderfood.model.TableModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ThemBanMoiActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton btnConfirm;
    private EditText edtTableName, edtTableSeats;
    private TextView tvTitle, tvSelectedArea;
    private TextView chipTang1, chipTang2, chipSanVuon;
    private SwitchMaterial switchStatus;
    
    private String selectedAreaName = "";
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_ban_moi);

        initViews();
        checkEditMode();
        setupListeners();
    }

    private void initViews() {
        // Find the title TextView inside the Toolbar
        View toolbar = findViewById(R.id.toolbar);
        if (toolbar instanceof androidx.appcompat.widget.Toolbar) {
            for (int i = 0; i < ((androidx.appcompat.widget.Toolbar)toolbar).getChildCount(); i++) {
                View v = ((androidx.appcompat.widget.Toolbar)toolbar).getChildAt(i);
                if (v instanceof TextView && !v.equals(findViewById(R.id.btn_back))) {
                    tvTitle = (TextView) v;
                    break;
                }
            }
        }

        btnBack = findViewById(R.id.btn_back);
        btnConfirm = findViewById(R.id.btn_confirm);
        edtTableName = findViewById(R.id.edt_table_name);
        edtTableSeats = findViewById(R.id.edt_table_seats);
        tvSelectedArea = findViewById(R.id.tv_selected_area);
        chipTang1 = findViewById(R.id.chip_tang1);
        chipTang2 = findViewById(R.id.chip_tang2);
        chipSanVuon = findViewById(R.id.chip_san_vuon);
        switchStatus = findViewById(R.id.switch_status);
    }

    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("IS_EDIT", false)) {
            isEditMode = true;
            if (tvTitle != null) tvTitle.setText("Chỉnh sửa bàn");
            btnConfirm.setText("CẬP NHẬT");

            String name = intent.getStringExtra("TABLE_ID");
            String area = intent.getStringExtra("TABLE_AREA");
            int seats = intent.getIntExtra("TABLE_SEATS", 4);

            edtTableName.setText(name);
            edtTableSeats.setText(String.valueOf(seats));
            selectedAreaName = area;
            tvSelectedArea.setText(area);
            tvSelectedArea.setTextColor(ContextCompat.getColor(this, R.color.text_primary));

            // Highlight the correct chip
            if ("Tầng 1".equals(area)) updateChipSelection(chipTang1);
            else if ("Tầng 2".equals(area)) updateChipSelection(chipTang2);
            else if ("Sân vườn".equals(area)) updateChipSelection(chipSanVuon);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        View.OnClickListener areaClickListener = v -> {
            updateChipSelection((TextView) v);
            selectedAreaName = ((TextView) v).getText().toString();
            tvSelectedArea.setText(selectedAreaName);
            tvSelectedArea.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        };

        chipTang1.setOnClickListener(areaClickListener);
        chipTang2.setOnClickListener(areaClickListener);
        chipSanVuon.setOnClickListener(areaClickListener);

        btnConfirm.setOnClickListener(v -> validateAndSave());
    }

    private void updateChipSelection(TextView selectedChip) {
        resetChips();
        selectedChip.setBackgroundResource(R.drawable.bg_tab_active_dark);
        selectedChip.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    private void resetChips() {
        TextView[] chips = {chipTang1, chipTang2, chipSanVuon};
        for (TextView chip : chips) {
            chip.setBackgroundResource(R.drawable.bg_tab_inactive);
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    private void validateAndSave() {
        String name = edtTableName.getText().toString().trim();
        String seatsStr = edtTableSeats.getText().toString().trim();

        if (name.isEmpty()) {
            edtTableName.setError("Tên bàn không được để trống");
            edtTableName.requestFocus();
            return;
        }

        if (seatsStr.isEmpty()) {
            edtTableSeats.setError("Vui lòng nhập sức chứa");
            edtTableSeats.requestFocus();
            return;
        }

        if (selectedAreaName.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn khu vực", Toast.LENGTH_SHORT).show();
            return;
        }

        int seats = Integer.parseInt(seatsStr);
        boolean isReady = switchStatus.isChecked();

        // Create Object
        TableModel table = new TableModel();
        table.setTableName(name);
        table.setSeats(seats);
        table.setArea(new Area(null, selectedAreaName));
        table.setOccupied(!isReady);

        String msg = isEditMode ? "Đã lấy đủ dữ liệu để cập nhật bàn: " : "Đã lấy đủ dữ liệu để thêm bàn: ";
        Toast.makeText(this, msg + name + ", chờ kết nối API server.", Toast.LENGTH_LONG).show();

        setResult(RESULT_OK);
        finish();
    }
}
