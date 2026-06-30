package com.example.apporderfood.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import com.google.android.material.card.MaterialCardView;
import com.example.apporderfood.Activity.InvoiceManageActivity;
import com.example.apporderfood.Activity.CancelManageActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.util.Pair;

import com.example.apporderfood.R;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.mikepenz.iconics.view.IconicsImageView;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
/**
 * Màn hình thống kê hoạt động của nhà hàng.
 *
 * Chức năng:
 * - Thống kê doanh thu.
 * - Thống kê số hóa đơn.
 * - Thống kê giá trị trung bình mỗi hóa đơn.
 * - Thống kê số món đã hủy.
 * - Lọc dữ liệu theo khoảng thời gian.
 */
public class ThongKeActivity extends AppCompatActivity {

    private IconicsImageView btnBack;
    private TextView tvTotalRevenue, tvTotalOrders, tvAverageValue, tvDateRange, tvTotalCancelledItems;
    private MaterialCardView cardTotalOrders, cardTotalCancelledItems;
    private LinearLayout btnPickDate;
    private View navOrder, navSoDo, navTienIch;
    private ZappyApiService apiService;
    private int currentResId = -1;
    private SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private String currentFromDate = "";
    private String currentToDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_thong_ke);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        apiService = RetrofitClient.getApiService();
        SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        currentResId = prefs.getInt("RES_ID", -1);
        initViews();
        setupListeners();
        // Mặc định load dữ liệu "Hôm nay"
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        String displayDate = displayFormat.format(today);
        tvDateRange.setText(displayDate);
        fetchPaidOrders(apiFormat.format(today), apiFormat.format(today));
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvAverageValue = findViewById(R.id.tvAverageValue);
        btnPickDate = findViewById(R.id.btnPickDate);
        tvDateRange = findViewById(R.id.tvDateRange);
        cardTotalOrders = findViewById(R.id.cardTotalOrders);
        tvTotalCancelledItems = findViewById(R.id.tvTotalCancelledItems);
        cardTotalCancelledItems = findViewById(R.id.cardTotalCancelledItems);
        navOrder = findViewById(R.id.navOrder);
        navSoDo = findViewById(R.id.navSoDo);
        navTienIch = findViewById(R.id.navTienIch);
    }
    /**
     * Khởi tạo các sự kiện cho giao diện.
     *
     * Bao gồm:
     * - Quay lại màn hình trước.
     * - Chọn khoảng thời gian thống kê.
     * - Mở danh sách hóa đơn.
     * - Mở danh sách món đã hủy.
     * - Điều hướng thanh menu.
     */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnPickDate.setOnClickListener(v -> showDateRangePicker());
        cardTotalOrders.setOnClickListener(v -> {
            Intent intent = new Intent(ThongKeActivity.this, InvoiceManageActivity.class);
            if (currentFromDate != null) intent.putExtra("FROM_DATE", currentFromDate);
            if (currentToDate != null) intent.putExtra("TO_DATE", currentToDate);
            startActivity(intent);
        });
        cardTotalCancelledItems.setOnClickListener(v -> {
            Intent intent = new Intent(ThongKeActivity.this, CancelManageActivity.class);
            if (!currentFromDate.isEmpty()) intent.putExtra("FROM_DATE", currentFromDate);
            if (!currentToDate.isEmpty()) intent.putExtra("TO_DATE", currentToDate);
            startActivity(intent);
        });
        navOrder.setOnClickListener(v -> {
            startActivity(new Intent(ThongKeActivity.this, DanhSachOrderActivity.class));
            overridePendingTransition(0, 0);
        });
        navSoDo.setOnClickListener(v -> {
            startActivity(new Intent(ThongKeActivity.this, SoDobanActivity.class));
            overridePendingTransition(0, 0);
        });
        navTienIch.setOnClickListener(v -> {
            startActivity(new Intent(ThongKeActivity.this, TienIchActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
    }
    /**
     * Hiển thị hộp thoại chọn khoảng thời gian thống kê.
     *
     * Sau khi người dùng chọn:
     * - Hiển thị khoảng thời gian.
     * - Tải lại dữ liệu thống kê.
     */
    private void showDateRangePicker() {
        // Tạo Date Range Picker
        MaterialDatePicker<Pair<Long, Long>> dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Chọn khoảng thời gian")
                        .build();
        // Người dùng xác nhận khoảng thời gian
        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            Long startDate = selection.first;
            Long endDate = selection.second;
            if (startDate != null && endDate != null) {
                Date start = new Date(startDate);
                Date end = new Date(endDate);

                String displayText = displayFormat.format(start);
                if (!displayFormat.format(start).equals(displayFormat.format(end))) {
                    displayText += " - " + displayFormat.format(end);
                }
                // Hiển thị khoảng thời gian lên giao diện
                tvDateRange.setText(displayText);
                fetchPaidOrders(apiFormat.format(start), apiFormat.format(end));
            }
        });
        dateRangePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }
    /**
     * Lấy dữ liệu thống kê theo khoảng thời gian.
     *
     * Bao gồm:
     * - Tổng doanh thu.
     * - Số hóa đơn.
     * - Giá trị trung bình mỗi hóa đơn.
     * - Số món đã hủy.
     *
     * @param fromDate ngày bắt đầu
     * @param toDate ngày kết thúc
     */
    private void fetchPaidOrders(String fromDate, String toDate) {
        if (currentResId == -1) return;
        // Lưu khoảng thời gian đang thống kê
        this.currentFromDate = fromDate;
        this.currentToDate = toDate;
        // Hiển thị trạng thái đang tải
        tvTotalRevenue.setText("Đang tính...");
        DecimalFormat numberFormat = new DecimalFormat("#,###");
        // Gọi API lấy danh sách hóa đơn đã thanh toán
        apiService.getPaidOrdersByRestaurant(currentResId, fromDate, toDate).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Tính tổng doanh thu và số lượng hóa đơn
                    List<Map<String, Object>> orders = response.body();
                    double totalRevenue = 0;
                    int totalOrders = orders.size();
                    for (Map<String, Object> order : orders) {
                        if (order.get("totalAmount") != null) {
                            // Cộng doanh thu của từng hóa đơn
                            totalRevenue += ((Number) order.get("totalAmount")).doubleValue();
                        }
                    }
                    // Tính giá trị trung bình mỗi hóa đơn
                    double avgValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;
                    // Hiển thị kết quả thống kê lên giao diện
                    tvTotalRevenue.setText(numberFormat.format(totalRevenue) + " đ");
                    tvTotalOrders.setText(totalOrders + " đơn");
                    tvAverageValue.setText(numberFormat.format(avgValue) + " đ");
                } else {
                    Toast.makeText(ThongKeActivity.this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(ThongKeActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
        // Hiển thị trạng thái đang tải số món hủy
        tvTotalCancelledItems.setText("...");
        // Gọi API lấy danh sách món đã hủy
        apiService.getCancelledOrdersByRestaurant(currentResId, fromDate, toDate).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                // Hiển thị tổng số món đã hủy
                if (response.isSuccessful() && response.body() != null) {
                    tvTotalCancelledItems.setText(response.body().size() + " món");
                } else {
                    tvTotalCancelledItems.setText("0 món");
                }
            }
            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                tvTotalCancelledItems.setText("0 món");
            }
        });
    }
}