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
    }

    private void setupRecyclerView() {
        adapter = new InvoiceManageAdapter(this);
        rvInvoices.setLayoutManager(new LinearLayoutManager(this));
        rvInvoices.setAdapter(adapter);
    }

    private void loadInvoices() {
        if (resId == -1) return;

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvInvoices.setVisibility(View.GONE);

        ZappyApiService api = RetrofitClient.getApiService();
        api.getPaidOrdersByRestaurant(resId).enqueue(new Callback<List<Map<String, Object>>>() {
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
