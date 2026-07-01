package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.R;
import com.example.apporderfood.adapter.OrderDetailAdapter;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.OrderDetail;
import com.example.apporderfood.model.CartItem;
import com.example.apporderfood.model.MenuItem;
import com.example.apporderfood.Activity.LapOrderActivity;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class XacNhanOrderActivity extends AppCompatActivity {

    private View btnBack;
    private LinearLayout btnGui;
    private LinearLayout btnThemMon;
    private View btnTinhTien;
    private RecyclerView rvOrderDetails;
    private ProgressBar progressBar;
    private TextView tvTitle;
    private TextView tvBadgeCount;
    private TextView tvItemCount;
    private TextView tvTotalAmount;

    private int orderId = -1;
    private int tableId = -1;
    private String tableName = "";

    private OrderDetailAdapter adapter;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_xac_nhan_order);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        orderId = getIntent().getIntExtra("ORDER_ID", -1);
        tableId = getIntent().getIntExtra("TABLE_ID", -1);
        tableName = getIntent().getStringExtra("TABLE_NAME") != null
                ? getIntent().getStringExtra("TABLE_NAME")
                : "Bàn";

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadOrderDetails(); // Gọi API lấy danh sách món
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnGui = findViewById(R.id.btnGui);
        btnThemMon = findViewById(R.id.btnThemMon);
        btnTinhTien = findViewById(R.id.btnTinhTien);
        rvOrderDetails = findViewById(R.id.rvOrderDetails);
        progressBar = findViewById(R.id.progressBar);
        tvTitle = findViewById(R.id.tvTitle);
        tvBadgeCount = findViewById(R.id.tvBadgeCount);
        tvItemCount = findViewById(R.id.tvItemCount);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);

        if (tvTitle != null) {
            tvTitle.setText("Xác nhận - " + tableName);
        }
    }

    private void setupRecyclerView() {
        android.content.SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        int userRole = prefs.getInt("ROLE", 0);
        boolean isAdmin = userRole == 1;
        boolean canCheckout = userRole == 1 || userRole == 2;

        if (!canCheckout && btnTinhTien != null) {
            btnTinhTien.setVisibility(View.GONE);
        }

        adapter = new OrderDetailAdapter(this, new java.util.ArrayList<>(), isAdmin, this::cancelItem);
        rvOrderDetails.setLayoutManager(new LinearLayoutManager(this));
        rvOrderDetails.setAdapter(adapter);
        rvOrderDetails.setNestedScrollingEnabled(false);
    }

    /**
     * HÀM XÓA/HUỶ MÓN ĂN
     * 
     * @param detail Món ăn cần xoá
     */
    private void cancelItem(OrderDetail detail) {
        // TRƯỜNG HỢP 1: Món chưa gửi bếp (ID = null vì món chỉ đang nằm trong
        // RAM/cartMap)
        if (detail.getId() == null) {
            if (detail.getMenuItem() != null && LapOrderActivity.cartMap.containsKey(detail.getMenuItem().getId())) {
                // Xoá trực tiếp khỏi biến static cartMap mà không cần gọi API
                LapOrderActivity.cartMap.remove(detail.getMenuItem().getId());
                Toast.makeText(XacNhanOrderActivity.this, "Đã xóa món khỏi giỏ!", Toast.LENGTH_SHORT).show();
                // Tải lại danh sách hiển thị
                loadOrderDetails();
            }
            return;
        }

        // TRƯỜNG HỢP 2: Món đã gửi bếp (ID != null), yêu cầu nhập lý do huỷ và gọi API
        // huỷ
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Nhập lý do huỷ món...");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 0);
        layout.addView(input);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy món")
                .setMessage("Hủy món " + (detail.getMenuItem() != null ? detail.getMenuItem().getItemName() : "") + "?")
                .setView(layout)
                .setPositiveButton("Hủy món", (dialog, which) -> {
                    String reason = input.getText().toString().trim();
                    progressBar.setVisibility(View.VISIBLE);
                    ZappyApiService api = RetrofitClient.getApiService();

                    api.cancelItem(detail.getId(), reason, new java.util.HashMap<>()).enqueue(new Callback<Map>() {
                        @Override
                        public void onResponse(Call<Map> call, Response<Map> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(XacNhanOrderActivity.this, "Đã hủy món!", Toast.LENGTH_SHORT).show();
                                loadOrderDetails();
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(XacNhanOrderActivity.this, "Lỗi hủy món", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(XacNhanOrderActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    /**
     * HÀM TẢI DANH SÁCH MÓN ĂN ĐỂ HIỂN THỊ LÊN MÀN HÌNH XÁC NHẬN
     * Bao gồm: Món đã có trong DB + Món vừa chọn thêm trong Giỏ hàng RAM (cartMap)
     */

    //B24 Gọi API xem bàn có món cũ không
    private void loadOrderDetails() {
        if (orderId == -1) {
            Toast.makeText(this, "Không tìm thấy đơn hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        rvOrderDetails.setVisibility(View.GONE);

        ZappyApiService api = RetrofitClient.getApiService();
        // Lấy các món đã lưu trong Database của Order này
        api.getOrderDetails(orderId).enqueue(new Callback<List<OrderDetail>>() {
            @Override
            public void onResponse(Call<List<OrderDetail>> call, Response<List<OrderDetail>> response) {
                progressBar.setVisibility(View.GONE);
                rvOrderDetails.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    List<OrderDetail> details = response.body();

                    // GỘP CÁC MÓN TRONG RAM VÀO DANH SÁCH HIỂN THỊ

                    //B25 Lấy danh sách món từ Ram gộp vô món cũ
                    for (CartItem ci : LapOrderActivity.cartMap.values()) {
                        OrderDetail localItem = new OrderDetail();
                        // Cực kỳ quan trọng: Set ID = null để Adapter biết đây là món mới, chưa lưu DB
                        localItem.setId(null);
                        localItem.setMenuItem(ci.getMenuItem());
                        localItem.setQuantity(ci.getQuantity());
                        localItem.setNote(ci.getNote());
                        BigDecimal price = ci.getMenuItem().getPrice();
                        localItem.setPriceAtSale(price);
                        // Set Status = 0 (Chưa gửi)
                        localItem.setStatus(0);

                        // Đẩy lên đầu danh sách
                        details.add(0, localItem);
                    }

                    adapter.setItems(details);
                    updateSummary(details);
                    updateGuiButton(details);
                } else {
                    Toast.makeText(XacNhanOrderActivity.this,
                            "Không tải được danh sách món!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<OrderDetail>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(XacNhanOrderActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSummary(List<OrderDetail> details) {
        BigDecimal total = BigDecimal.ZERO;
        int totalQty = 0;

        for (OrderDetail d : details) {
            if (d.getSubTotal() != null) {
                total = total.add(d.getSubTotal());
            }
            if (d.getQuantity() != null) {
                totalQty += d.getQuantity();
            }
        }

        tvTotalAmount.setText(formatter.format(total));
        tvItemCount.setText(totalQty + " món");
        tvBadgeCount.setText(details.size() + " MÓN");
    }

    private void updateGuiButton(List<OrderDetail> details) {
        boolean hasPending = false;
        for (OrderDetail d : details) {
            if (d.getStatus() != null && d.getStatus() == 0) {
                hasPending = true;
                break;
            }
        }
        btnGui.setEnabled(hasPending);
        if (hasPending) {
            btnGui.setBackgroundResource(R.drawable.bg_btn_primary);
            btnGui.setAlpha(1.0f);
        } else {
            btnGui.setBackgroundResource(R.drawable.bg_btn_disabled);
            btnGui.setAlpha(1.0f);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrderDetails();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    private void setupClickListeners() {

        btnBack.setOnClickListener(v -> finish());

        btnThemMon.setOnClickListener(v -> {
            Intent intent = new Intent(this, LapOrderActivity.class);
            intent.putExtra("TABLE_ID", tableId);
            intent.putExtra("TABLE_NAME", tableName);
            startActivity(intent);
        });

        //B29 Bấm vào nút gửi để họi hàm sendOrder()

        btnGui.setOnClickListener(v -> sendOrder());

        if (btnTinhTien != null) {
            btnTinhTien.setOnClickListener(v -> {
                Intent intent = new Intent(this, HoaDonActivity.class);
                intent.putExtra("ORDER_ID", orderId);
                intent.putExtra("TABLE_NAME", tableName);
                startActivity(intent);
            });
        }
    }

    /**
     * HÀM XỬ LÝ KHI BẤM NÚT "GỬI BẾP"
     */

    //B30 Gọi hàm này khi bấm nút gửi
    private void sendOrder() {
        if (orderId == -1)
            return;
        btnGui.setEnabled(false);
        btnGui.setAlpha(0.45f);
        ZappyApiService api = RetrofitClient.getApiService();

        // Nếu trong giỏ hàng tạm có món, ta cần phải lưu vào DB trước khi gọi lệnh gửi
        // bếp
        if (!LapOrderActivity.cartMap.isEmpty()) {
            List<Map<String, Object>> batchData = new ArrayList<>();
            // Duyệt qua cartMap để đóng gói dữ liệu thành danh sách JSON
            //B31 Dùng vòng lặp chuyển Map thành JSON
            for (CartItem ci : LapOrderActivity.cartMap.values()) {
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("itemId", ci.getMenuItem().getId());
                item.put("quantity", ci.getQuantity());
                item.put("note", ci.getNote() != null ? ci.getNote() : "");
                batchData.add(item);
            }
            // B32 Gọi API lưu hàng loạt món vào DB (Batch Insert)
            api.addBatchItems(orderId, batchData).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        executeSendOrderAPI(api);
                    } else {
                        btnGui.setEnabled(true);
                        btnGui.setAlpha(1.0f);
                        Toast.makeText(XacNhanOrderActivity.this, "Lỗi thêm món trước khi gửi!", Toast.LENGTH_SHORT)
                                .show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    btnGui.setEnabled(true);
                    btnGui.setAlpha(1.0f);
                    Toast.makeText(XacNhanOrderActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            executeSendOrderAPI(api);
        }
    }

    /**
     * HÀM CHỐT "GỬI BẾP" VÀ ĐỔI TRẠNG THÁI MÓN TRONG DB
     */

    //B33 Sau khi thêm thành công, gọi API sendOrder() để ổi trạng thái thành "ĐÃ GỬI"
    private void executeSendOrderAPI(ZappyApiService api) {
        api.sendOrder(orderId).enqueue(new Callback<java.util.Map>() {
            @Override
            public void onResponse(Call<java.util.Map> call, Response<java.util.Map> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(XacNhanOrderActivity.this,
                            "Đã gửi lên bếp thành công!", Toast.LENGTH_SHORT).show();

                    // B34 chạy lệnh để Dọn sạch giỏ hàng tạm trên RAM sau khi gửi thành công
                    LapOrderActivity.cartMap.clear();
                    if (LapOrderActivity.instance != null) {
                        LapOrderActivity.instance.finish();
                    }

                    //B35 Intent chuyển sang màn hình ChiTietBan và kết thúc
                    Intent intent = new Intent(XacNhanOrderActivity.this, ChiTietBanActivity.class);
                    intent.putExtra("ORDER_ID", orderId);
                    intent.putExtra("TABLE_ID", tableId);
                    intent.putExtra("TABLE_NAME", tableName);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    btnGui.setEnabled(true);
                    btnGui.setAlpha(1.0f);
                    Toast.makeText(XacNhanOrderActivity.this,
                            "Gửi đơn thất bại, thử lại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<java.util.Map> call, Throwable t) {
                btnGui.setEnabled(true);
                btnGui.setAlpha(1.0f);
                Toast.makeText(XacNhanOrderActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
