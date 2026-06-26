package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apporderfood.R;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.view.View;
import android.widget.TextView;
import android.content.SharedPreferences;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.adapter.InvoiceItemAdapter;
import com.example.apporderfood.model.OrderDetail;

/**
 * HoaDonActivity - Thanh toán & Đóng hóa đơn
 * Layout: activity_hoa_don.xml
 * Flow:
 *  1. Hiển thị tổng tiền = SUM(quantity * price_at_sale) cho món status = 1
 *  2. Nút "THANH TOÁN" -> Gọi API POST /api/orders/{id}/checkout
 *     Backend: Orders.status = 1 (đã thanh toán) + Tables.is_occupied = FALSE
 *  3. Sau thanh toán -> Về SoDobanActivity (bàn đã trống)
 */
public class HoaDonActivity extends AppCompatActivity {

    private View btnBack;
    private View btnThanhToan;
    
    private TextView tvInvoiceDate;
    private TextView tvTableName;
    private TextView tvStaffName;
    private TextView tvTotal;
    private RecyclerView rvInvoiceItems;
    
    private InvoiceItemAdapter adapter;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    private int orderId = -1;
    private String tableName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hoa_don);

        orderId   = getIntent().getIntExtra("ORDER_ID", -1);
        tableName = getIntent().getStringExtra("TABLE_NAME") != null
                ? getIntent().getStringExtra("TABLE_NAME") : "Bàn";

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnBack      = findViewById(R.id.btnBack);
        btnThanhToan = findViewById(R.id.btnConfirmPayment);
        
        tvInvoiceDate = findViewById(R.id.tvInvoiceDate);
        tvTableName   = findViewById(R.id.tvTableName);
        tvStaffName   = findViewById(R.id.tvStaffName);
        tvTotal       = findViewById(R.id.tvTotal);
        rvInvoiceItems = findViewById(R.id.rvInvoiceItems);
        
        tvTableName.setText(tableName);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy (HH:mm)", new Locale("vi", "VN"));
        tvInvoiceDate.setText(sdf.format(new Date()));
        
        SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        String fullname = prefs.getString("FULLNAME", "Nhân viên");
        tvStaffName.setText(fullname);
        
        adapter = new InvoiceItemAdapter(this, new java.util.ArrayList<>());
        rvInvoiceItems.setLayoutManager(new LinearLayoutManager(this));
        rvInvoiceItems.setAdapter(adapter);
        
        loadInvoiceData();
    }
    
    private void loadInvoiceData() {
        if (orderId == -1) return;
        
        ZappyApiService api = RetrofitClient.getApiService();
        api.getOrderDetails(orderId).enqueue(new Callback<List<OrderDetail>>() {
            @Override
            public void onResponse(Call<List<OrderDetail>> call, Response<List<OrderDetail>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<OrderDetail> details = response.body();
                    
                    List<OrderDetail> validItems = new java.util.ArrayList<>();
                    BigDecimal total = BigDecimal.ZERO;
                    
                    for (OrderDetail d : details) {
                        if (d.getStatus() != null && d.getStatus() != 2) { // Không tính món đã hủy
                            validItems.add(d);
                            if (d.getSubTotal() != null) {
                                total = total.add(d.getSubTotal());
                            }
                        }
                    }
                    
                    adapter.setItems(validItems);
                    tvTotal.setText(formatter.format(total) + "đ");
                }
            }

            @Override
            public void onFailure(Call<List<OrderDetail>> call, Throwable t) {
                Toast.makeText(HoaDonActivity.this, "Lỗi tải dữ liệu hóa đơn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {

        // Nút Back -> Về chi tiết bàn
        btnBack.setOnClickListener(v -> finish());

        // Nút THANH TOÁN -> Gọi API checkout
        btnThanhToan.setOnClickListener(v -> checkout());
    }

    /**
     * Gọi API POST /api/orders/{orderId}/checkout
     * Backend sẽ:
     *  - Tính Orders.total_amount = SUM(quantity * price_at_sale) với status=1
     *  - Đặt Orders.status = 1 (đã thanh toán)
     *  - Đặt Tables.is_occupied = FALSE (bàn trống trở lại)
     */
    private void checkout() {
        if (orderId == -1) {
            Toast.makeText(this, "Không tìm thấy hóa đơn!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnThanhToan.setEnabled(false);
        ZappyApiService api = RetrofitClient.getApiService();

        api.checkout(orderId).enqueue(new Callback<Map>() {
            @Override
            public void onResponse(Call<Map> call, Response<Map> response) {
                btnThanhToan.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(HoaDonActivity.this,
                            "Thanh toán thành công! " + tableName + " đã trống.",
                            Toast.LENGTH_LONG).show();
                    // Về SoDobanActivity, bàn đã được giải phóng
                    Intent intent = new Intent(HoaDonActivity.this, SoDobanActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(HoaDonActivity.this,
                            "Thanh toán thất bại, thử lại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map> call, Throwable t) {
                btnThanhToan.setEnabled(true);
                Toast.makeText(HoaDonActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
