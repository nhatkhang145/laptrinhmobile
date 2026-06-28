package com.example.apporderfood.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.apporderfood.R;
import com.example.apporderfood.adapter.CancelManageAdapter;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CancelManageActivity extends AppCompatActivity {

    private View btnBack;
    private RecyclerView rvCancelList;
    private ProgressBar pbLoading;
    private View layoutEmpty;

    private CancelManageAdapter adapter;
    private int resId = -1;
    
    private String fromDateStr = null;
    private String toDateStr = null;
    private TextView tvFromDate, tvToDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cancel_manage);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("ZappySession", Context.MODE_PRIVATE);
        resId = prefs.getInt("RES_ID", -1);

        initViews();
        
        android.content.Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("FROM_DATE")) {
                fromDateStr = intent.getStringExtra("FROM_DATE");
                if (fromDateStr != null && fromDateStr.length() >= 10) {
                    String[] parts = fromDateStr.substring(0, 10).split("-");
                    if (parts.length == 3) tvFromDate.setText(parts[2] + "/" + parts[1] + "/" + parts[0]);
                }
            }
            if (intent.hasExtra("TO_DATE")) {
                toDateStr = intent.getStringExtra("TO_DATE");
                if (toDateStr != null && toDateStr.length() >= 10) {
                    String[] parts = toDateStr.substring(0, 10).split("-");
                    if (parts.length == 3) tvToDate.setText(parts[2] + "/" + parts[1] + "/" + parts[0]);
                }
            }
        }
        
        setupRecyclerView();
        
        btnBack.setOnClickListener(v -> finish());
        
        loadCancelledItems();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvCancelList = findViewById(R.id.rvCancelList);
        pbLoading = findViewById(R.id.pbLoading);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        
        tvFromDate = findViewById(R.id.tvFromDate);
        tvToDate = findViewById(R.id.tvToDate);
        
        findViewById(R.id.btnFromDate).setOnClickListener(v -> showDatePicker(true));
        findViewById(R.id.btnToDate).setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isFromDate) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String dateStr = String.format(java.util.Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            String displayStr = String.format(java.util.Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
            if (isFromDate) {
                fromDateStr = dateStr + "T00:00:00";
                tvFromDate.setText(displayStr);
            } else {
                toDateStr = dateStr + "T23:59:59";
                tvToDate.setText(displayStr);
            }
            loadCancelledItems();
        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }

    private void setupRecyclerView() {
        adapter = new CancelManageAdapter(this, new java.util.ArrayList<>());
        rvCancelList.setLayoutManager(new LinearLayoutManager(this));
        rvCancelList.setAdapter(adapter);
    }

    private void loadCancelledItems() {
        if (resId == -1) return;

        pbLoading.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        rvCancelList.setVisibility(View.GONE);

        ZappyApiService api = RetrofitClient.getApiService();
        api.getCancelledOrdersByRestaurant(resId, fromDateStr, toDateStr).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                pbLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> list = response.body();
                    if (list.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                    } else {
                        rvCancelList.setVisibility(View.VISIBLE);
                        adapter.updateData(list);
                    }
                } else {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(CancelManageActivity.this, "Không có dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                pbLoading.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(CancelManageActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
