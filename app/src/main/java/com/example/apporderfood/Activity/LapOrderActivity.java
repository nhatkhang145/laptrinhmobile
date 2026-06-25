package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apporderfood.R;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * LapOrderActivity - Màn hình chọn món khi ấn vào bàn TRỐNG
 * Layout: activity_lap_order.xml
 * Flow:
 *  1. Nhận TABLE_ID, TABLE_NAME từ SoDobanActivity
 *  2. Gọi API POST /api/orders/open để tạo hóa đơn mới, bàn -> is_occupied = TRUE
 *  3. NV chọn món (thêm vào giỏ RAM tạm)
 *  4. Nút ĐỒNG Ý -> Gọi API thêm từng món -> Chuyển sang XacNhanOrderActivity
 *  5. Nút HỦY BỎ -> finish() về SoDobanActivity
 */
public class LapOrderActivity extends AppCompatActivity {

    private LinearLayout btnHuyBo;
    private LinearLayout btnDongY;
    private LinearLayout btnAddItem1;
    private LinearLayout btnAddItem2;
    private LinearLayout btnAddItem3;
    private LinearLayout btnAddItem4;
    private TextView tabHayDung;
    private TextView tabMonChinh;
    private TextView tabDoUong;

    private int tableId = -1;
    private String tableName = "";
    private int orderId = -1; // Sẽ được set sau khi gọi API openTable thành công

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lap_order);

        // Nhận dữ liệu từ SoDobanActivity
        tableId   = getIntent().getIntExtra("TABLE_ID", -1);
        tableName = getIntent().getStringExtra("TABLE_NAME") != null
                ? getIntent().getStringExtra("TABLE_NAME") : "Bàn";

        initViews();
        setupClickListeners();
        openTable(); // Bước 1: Tạo hóa đơn ngay khi mở màn hình
    }

    private void initViews() {
        btnHuyBo   = findViewById(R.id.btnHuyBo);
        btnDongY   = findViewById(R.id.btnDongY);
        btnAddItem1 = findViewById(R.id.btnAddItem1);
        btnAddItem2 = findViewById(R.id.btnAddItem2);
        btnAddItem3 = findViewById(R.id.btnAddItem3);
        btnAddItem4 = findViewById(R.id.btnAddItem4);
        tabHayDung  = findViewById(R.id.tabHayDung);
        tabMonChinh = findViewById(R.id.tabMonChinh);
        tabDoUong   = findViewById(R.id.tabDoUong);
    }

    /**
     * Bước 1: Gọi API POST /api/orders/open để tạo hóa đơn mới.
     * Backend sẽ: tạo dòng trong Orders + đặt Tables.is_occupied = TRUE
     */
    private void openTable() {
        if (tableId == -1) return;

        ZappyApiService api = RetrofitClient.getApiService();
        Map<String, Integer> data = new HashMap<>();
        data.put("tableId", tableId);

        api.openTable(data).enqueue(new Callback<Map>() {
            @Override
            public void onResponse(Call<Map> call, Response<Map> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Object id = response.body().get("orderId");
                    if (id != null) {
                        orderId = ((Double) id).intValue();
                    }
                } else {
                    Toast.makeText(LapOrderActivity.this,
                            "Không thể mở bàn, thử lại!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Map> call, Throwable t) {
                Toast.makeText(LapOrderActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setupClickListeners() {

        // Nút HỦY BỎ -> quay lại sơ đồ bàn
        btnHuyBo.setOnClickListener(v -> finish());

        // Nút ĐỒNG Ý -> Chuyển sang màn hình xác nhận / chi tiết order
        btnDongY.setOnClickListener(v -> {
            if (orderId == -1) {
                Toast.makeText(this, "Đang tạo đơn, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, XacNhanOrderActivity.class);
            intent.putExtra("ORDER_ID", orderId);
            intent.putExtra("TABLE_NAME", tableName);
            intent.putExtra("TABLE_ID", tableId);
            startActivity(intent);
            finish();
        });

        // Nút thêm từng món vào đơn (TODO: tích hợp API addItem)
        btnAddItem1.setOnClickListener(v ->
                Toast.makeText(this, "Đã thêm: Mì Kim Chi Bò Mỹ", Toast.LENGTH_SHORT).show());
        btnAddItem2.setOnClickListener(v ->
                Toast.makeText(this, "Đã thêm: Khăn Lạnh", Toast.LENGTH_SHORT).show());
        btnAddItem3.setOnClickListener(v ->
                Toast.makeText(this, "Đã thêm: Trà Chanh Sả", Toast.LENGTH_SHORT).show());
        btnAddItem4.setOnClickListener(v ->
                Toast.makeText(this, "Đã thêm: Salad Ức Gà", Toast.LENGTH_SHORT).show());

        // Tabs danh mục
        tabHayDung.setOnClickListener(v ->
                Toast.makeText(this, "Hay dùng", Toast.LENGTH_SHORT).show());
        tabMonChinh.setOnClickListener(v ->
                Toast.makeText(this, "Món chính", Toast.LENGTH_SHORT).show());
        tabDoUong.setOnClickListener(v ->
                Toast.makeText(this, "Đồ uống", Toast.LENGTH_SHORT).show());
    }
}
