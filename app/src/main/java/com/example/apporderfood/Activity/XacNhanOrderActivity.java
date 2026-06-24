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
 * XacNhanOrderActivity - Màn hình xem danh sách món đã gọi & Gửi đơn
 * Layout: activity_xac_nhan_order.xml
 * Flow:
 *  - Hiển thị danh sách Order_Details của orderId
 *  - Nút "Thêm món" -> LapOrderActivity để chọn thêm
 *  - Nút "GỬI"     -> Gọi API PUT /api/orders/{id}/send
 *                      -> status = 1 (KHÓA, NV không sửa/xóa được nữa)
 *                      -> Chuyển sang ChiTietBanActivity
 *  - Nút Back      -> Về SoDobanActivity
 */
public class XacNhanOrderActivity extends AppCompatActivity {

    private LinearLayout btnBack;
    private LinearLayout btnGui;
    private LinearLayout btnThemMon;

    private int orderId   = -1;
    private int tableId   = -1;
    private String tableName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xac_nhan_order);

        orderId   = getIntent().getIntExtra("ORDER_ID", -1);
        tableId   = getIntent().getIntExtra("TABLE_ID", -1);
        tableName = getIntent().getStringExtra("TABLE_NAME") != null
                ? getIntent().getStringExtra("TABLE_NAME") : "Bàn";

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnBack    = findViewById(R.id.btnBack);
        btnGui     = findViewById(R.id.btnGui);
        btnThemMon = findViewById(R.id.btnThemMon);
    }

    private void setupClickListeners() {

        // Back -> Về SoDobanActivity
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, SoDobanActivity.class));
            finish();
        });

        // Thêm món -> Quay lại LapOrderActivity với cùng orderId
        btnThemMon.setOnClickListener(v -> {
            Intent intent = new Intent(this, LapOrderActivity.class);
            intent.putExtra("TABLE_ID", tableId);
            intent.putExtra("TABLE_NAME", tableName);
            startActivity(intent);
        });

        // GỬI -> Gọi API sendOrder -> status = 1 -> sang ChiTietBanActivity
        btnGui.setOnClickListener(v -> sendOrder());
    }

    /**
     * Gọi API PUT /api/orders/{orderId}/send
     * Kết quả: Order_Details.status = 1 (khóa chỉnh sửa của NV)
     */
    private void sendOrder() {
        if (orderId == -1) {
            Toast.makeText(this, "Không tìm thấy đơn hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGui.setEnabled(false);
        ZappyApiService api = RetrofitClient.getApiService();

        api.sendOrder(orderId).enqueue(new Callback<Map>() {
            @Override
            public void onResponse(Call<Map> call, Response<Map> response) {
                btnGui.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(XacNhanOrderActivity.this,
                            "Đã gửi đơn lên bếp!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(XacNhanOrderActivity.this, ChiTietBanActivity.class);
                    intent.putExtra("ORDER_ID", orderId);
                    intent.putExtra("TABLE_ID", tableId);
                    intent.putExtra("TABLE_NAME", tableName);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(XacNhanOrderActivity.this,
                            "Gửi đơn thất bại, thử lại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map> call, Throwable t) {
                btnGui.setEnabled(true);
                Toast.makeText(XacNhanOrderActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
