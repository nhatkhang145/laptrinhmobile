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
 * ChiTietBanActivity - Chi tiết đơn đang phục vụ tại bàn
 * Layout: activity_chi_tiet_ban.xml
 * Flow:
 *  - Hiển thị danh sách Order_Details theo orderId
 *  - Nút "Thêm món" -> LapOrderActivity (NV thêm thêm món mới, status=0)
 *  - Nút "Tính tiền" -> HoaDonActivity (Thanh toán, đóng hóa đơn)
 *  - Nút Hủy món (chỉ Quản lý) -> Gọi API cancelItem (status -> 2)
 *  - Nút Back -> DanhSachOrderActivity
 */
public class ChiTietBanActivity extends AppCompatActivity {

    private LinearLayout btnBack;
    private LinearLayout btnThemMon;
    private LinearLayout btnTinhTien;

    private int orderId   = -1;
    private int tableId   = -1;
    private String tableName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_ban);

        orderId   = getIntent().getIntExtra("ORDER_ID", -1);
        tableId   = getIntent().getIntExtra("TABLE_ID", -1);
        tableName = getIntent().getStringExtra("TABLE_NAME") != null
                ? getIntent().getStringExtra("TABLE_NAME") : "Bàn";

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnBack     = findViewById(R.id.btnBack);
        btnThemMon  = findViewById(R.id.btnThemMon);
        btnTinhTien = findViewById(R.id.btnTinhTien);
    }

    private void setupClickListeners() {

        // Back -> Về danh sách order
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, DanhSachOrderActivity.class));
            finish();
        });

        // Thêm món -> Lập order thêm cho bàn này
        btnThemMon.setOnClickListener(v -> {
            Intent intent = new Intent(this, LapOrderActivity.class);
            intent.putExtra("TABLE_ID", tableId);
            intent.putExtra("TABLE_NAME", tableName);
            startActivity(intent);
        });

        // Tính tiền -> Thanh toán
        btnTinhTien.setOnClickListener(v -> {
            Intent intent = new Intent(this, HoaDonActivity.class);
            intent.putExtra("ORDER_ID", orderId);
            intent.putExtra("TABLE_NAME", tableName);
            startActivity(intent);
        });
    }
}
