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
import java.util.Map;

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

    private LinearLayout btnBack;
    private LinearLayout btnThanhToan;

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
