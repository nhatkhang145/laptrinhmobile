package com.example.apporderfood.Activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apporderfood.R;
import com.mikepenz.iconics.view.IconicsImageView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ShiftManageActivity extends AppCompatActivity {

    private IconicsImageView btnBack;
    private IconicsImageView btnHistory;
    private ProgressBar progressBar;
    
    // View Chưa có ca làm
    private LinearLayout layoutOpenShift;
    private EditText edtStartingFund;
    private LinearLayout btnSelectEmployees;
    private TextView tvSelectedEmployees;
    private LinearLayout btnOpenShift;
    
    // View Đang có ca làm
    private LinearLayout layoutActiveShift;
    private TextView tvStartTime, tvStartingFund, tvActiveEmployees, tvOrderCount, tvTotalRevenue;
    private LinearLayout btnCloseShift;

    // Biến giả lập trạng thái
    private boolean isShiftOpen = false;
    private double currentFund = 0;
    private String startTimeStr = "";
    private String selectedEmployeeNames = "";
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quan_ly_ca);

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();

        btnBack.setOnClickListener(v -> finish());
        
        btnHistory.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng xem Lịch sử ca làm đang phát triển", Toast.LENGTH_SHORT).show();
            // Tương lai: Mở ShiftHistoryActivity
        });

        btnOpenShift.setOnClickListener(v -> handleOpenShift());
        
        btnSelectEmployees.setOnClickListener(v -> {
            // Tương lai: Mở Dialog chứa CheckBox danh sách nhân viên từ API
            selectedEmployeeNames = "Nguyễn Văn A, Lê Thị B";
            tvSelectedEmployees.setText(selectedEmployeeNames);
            Toast.makeText(this, "Đã chọn: " + selectedEmployeeNames, Toast.LENGTH_SHORT).show();
        });
        
        btnCloseShift.setOnClickListener(v -> showCloseShiftDialog());

        // Lấy trạng thái từ Backend (giả lập)
        loadShiftState();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnHistory = findViewById(R.id.btnHistory);
        progressBar = findViewById(R.id.progressBar);
        
        layoutOpenShift = findViewById(R.id.layoutOpenShift);
        edtStartingFund = findViewById(R.id.edtStartingFund);
        btnSelectEmployees = findViewById(R.id.btnSelectEmployees);
        tvSelectedEmployees = findViewById(R.id.tvSelectedEmployees);
        btnOpenShift = findViewById(R.id.btnOpenShift);
        
        layoutActiveShift = findViewById(R.id.layoutActiveShift);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvStartingFund = findViewById(R.id.tvStartingFund);
        tvActiveEmployees = findViewById(R.id.tvActiveEmployees);
        tvOrderCount = findViewById(R.id.tvOrderCount);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        btnCloseShift = findViewById(R.id.btnCloseShift);
    }

    private void loadShiftState() {
        // Tương lai: Gọi API kiểm tra xem có ca nào đang active không
        progressBar.setVisibility(View.VISIBLE);
        layoutOpenShift.setVisibility(View.GONE);
        layoutActiveShift.setVisibility(View.GONE);

        // Giả lập load API
        progressBar.postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            updateUI();
        }, 500);
    }

    private void handleOpenShift() {
        String fundStr = edtStartingFund.getText().toString().trim();
        if (fundStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiền quỹ đầu ca", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedEmployeeNames.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn nhân viên cho ca làm", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            currentFund = Double.parseDouble(fundStr);
            isShiftOpen = true;
            
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm (dd/MM/yyyy)", Locale.getDefault());
            startTimeStr = sdf.format(new Date());

            // Tương lai: Gọi API POST /shifts
            Toast.makeText(this, "Mở ca làm việc thành công!", Toast.LENGTH_SHORT).show();
            updateUI();
        } catch (Exception e) {
            Toast.makeText(this, "Tiền quỹ không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCloseShiftDialog() {
        // Tương lai: Gọi API lấy thống kê tổng doanh thu của ca để xác nhận trước khi đóng
        double mockTotalRevenue = 2500000;
        int mockOrderCount = 12;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("XÁC NHẬN ĐÓNG CA LÀM");
        
        String msg = "Thời gian mở: " + startTimeStr + "\n\n"
                + "Quỹ đầu ca: " + formatter.format(currentFund) + "đ\n"
                + "Số đơn đã thu: " + mockOrderCount + " đơn\n"
                + "Doanh thu trong ca: " + formatter.format(mockTotalRevenue) + "đ\n"
                + "---------------------------------\n"
                + "TỔNG TIỀN MẶT KẾT THÚC: " + formatter.format(currentFund + mockTotalRevenue) + "đ\n\n"
                + "Bạn có chắc chắn muốn kết thúc phiên làm việc này không?";
                
        builder.setMessage(msg);
        
        builder.setPositiveButton("ĐÓNG CA", (dialog, which) -> {
            // Tương lai: Gọi API PUT /shifts/{id} để đổi trạng thái thành Closed
            isShiftOpen = false;
            currentFund = 0;
            selectedEmployeeNames = "";
            edtStartingFund.setText("");
            tvSelectedEmployees.setText("");
            Toast.makeText(this, "Đã đóng ca làm việc", Toast.LENGTH_SHORT).show();
            updateUI();
        });
        
        builder.setNegativeButton("HỦY", (dialog, which) -> dialog.dismiss());
        
        builder.show();
    }

    private void updateUI() {
        if (isShiftOpen) {
            layoutOpenShift.setVisibility(View.GONE);
            layoutActiveShift.setVisibility(View.VISIBLE);
            
            tvStartTime.setText(startTimeStr);
            tvStartingFund.setText(formatter.format(currentFund) + "đ");
            tvActiveEmployees.setText(selectedEmployeeNames);
            
            // Giả lập dữ liệu thu được
            tvOrderCount.setText("12");
            tvTotalRevenue.setText("2.500.000đ");
        } else {
            layoutOpenShift.setVisibility(View.VISIBLE);
            layoutActiveShift.setVisibility(View.GONE);
        }
    }
}
