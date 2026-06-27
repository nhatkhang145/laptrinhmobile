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

import com.example.apporderfood.R;
import com.example.apporderfood.adapter.InvoiceManageAdapter;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceManageActivity extends AppCompatActivity {

    private View btnBack;
    private RecyclerView rvInvoices;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private InvoiceManageAdapter adapter;
    private int resId = -1;
    
    private String fromDateStr = null;
    private String toDateStr = null;
    private TextView tvFromDate, tvToDate;
    private View btnClearFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_manage);

        SharedPreferences prefs = getSharedPreferences("ZappySession", Context.MODE_PRIVATE);
        resId = prefs.getInt("RES_ID", -1);

        initViews();
        setupRecyclerView();
        
        btnBack.setOnClickListener(v -> finish());
        
        loadInvoices();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvInvoices = findViewById(R.id.rvInvoices);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        
        tvFromDate = findViewById(R.id.tvFromDate);
        tvToDate = findViewById(R.id.tvToDate);
        btnClearFilter = findViewById(R.id.btnClearFilter);
        
        findViewById(R.id.btnFromDate).setOnClickListener(v -> showDatePicker(true));
        findViewById(R.id.btnToDate).setOnClickListener(v -> showDatePicker(false));
        btnClearFilter.setOnClickListener(v -> {
            fromDateStr = null;
            toDateStr = null;
            tvFromDate.setText("Từ ngày");
            tvToDate.setText("Đến ngày");
            btnClearFilter.setVisibility(View.GONE);
            loadInvoices();
        });
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
            btnClearFilter.setVisibility(View.VISIBLE);
            loadInvoices();
        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }

    private void setupRecyclerView() {
        adapter = new InvoiceManageAdapter(this, invoice -> {
            android.content.Intent intent = new android.content.Intent(InvoiceManageActivity.this, HoaDonActivity.class);
            if (invoice.get("id") != null) {
                intent.putExtra("ORDER_ID", ((Number) invoice.get("id")).intValue());
            }
            if (invoice.get("table") != null) {
                Map<String, Object> table = (Map<String, Object>) invoice.get("table");
                String areaName = table.get("area") != null ? (String) ((Map<String, Object>) table.get("area")).get("areaName") : "Khu vực";
                intent.putExtra("TABLE_NAME", areaName + " - " + table.get("tableName"));
            }
            intent.putExtra("IS_VIEW_ONLY", true);
            startActivity(intent);
        });
        rvInvoices.setLayoutManager(new LinearLayoutManager(this));
        rvInvoices.setAdapter(adapter);
    }

    private void loadInvoices() {
        if (resId == -1) return;

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvInvoices.setVisibility(View.GONE);

        ZappyApiService api = RetrofitClient.getApiService();
        api.getPaidOrdersByRestaurant(resId, fromDateStr, toDateStr).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> invoices = response.body();
                    
                    adapter.setInvoices(invoices);
                    
                    if (invoices.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvInvoices.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvInvoices.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(InvoiceManageActivity.this, "Lỗi khi lấy danh sách hóa đơn", Toast.LENGTH_SHORT).show();
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(InvoiceManageActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }
}
