package com.example.apporderfood.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.R;
import com.example.apporderfood.adapter.ShiftManageAdapter;
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShiftManageActivity extends AppCompatActivity {

    private IconicsImageView btnBack;
    private RecyclerView rvShifts;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private View btnAddShift;

    private ShiftManageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift_manage);

        initViews();
        setupRecyclerView();

        btnBack.setOnClickListener(v -> finish());

        btnAddShift.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng Thêm ca làm đang phát triển", Toast.LENGTH_SHORT).show();
        });

        loadMockData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvShifts = findViewById(R.id.rvShifts);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnAddShift = findViewById(R.id.btnAddShift);
    }

    private void setupRecyclerView() {
        adapter = new ShiftManageAdapter(this);
        rvShifts.setLayoutManager(new LinearLayoutManager(this));
        rvShifts.setAdapter(adapter);
    }

    private void loadMockData() {
        // Hien tai backend chua co API cho Shift, nen dung du lieu gia
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvShifts.setVisibility(View.GONE);

        // Gia lap loading
        rvShifts.postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            List<Map<String, String>> mockShifts = new ArrayList<>();
            
            Map<String, String> shift1 = new HashMap<>();
            shift1.put("name", "Ca Sáng");
            shift1.put("time", "06:00 - 14:00");
            mockShifts.add(shift1);
            
            Map<String, String> shift2 = new HashMap<>();
            shift2.put("name", "Ca Chiều");
            shift2.put("time", "14:00 - 22:00");
            mockShifts.add(shift2);
            
            Map<String, String> shift3 = new HashMap<>();
            shift3.put("name", "Ca Tối (Part-time)");
            shift3.put("time", "18:00 - 22:00");
            mockShifts.add(shift3);

            adapter.setShifts(mockShifts);
            rvShifts.setVisibility(View.VISIBLE);
        }, 500);
    }
}
