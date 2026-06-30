package com.example.apporderfood.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.apporderfood.R;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.mikepenz.iconics.view.IconicsImageView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuanLyCaActivity extends AppCompatActivity {

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

    // Biến trạng thái
    private boolean isShiftOpen = false;
    private int currentShiftId = -1;
    private double currentFund = 0;
    private String startTimeStr = "";
    private String selectedEmployeeNames = "";
    private String selectedEmployeeIds = "";
    private double currentTotalRevenue = 0;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    private List<com.example.apporderfood.model.User> allUsers = new java.util.ArrayList<>();
    private boolean[] selectedUserItems;

    private ZappyApiService apiService;
    private int resId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quan_ly_ca);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        apiService = RetrofitClient.getApiService();
        SharedPreferences prefs = getSharedPreferences("ZappySession", Context.MODE_PRIVATE);
        resId = prefs.getInt("RES_ID", -1);

        initViews();

        btnBack.setOnClickListener(v -> finish());
        
        btnHistory.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, LichSuCaActivity.class));
        });

        btnOpenShift.setOnClickListener(v -> handleOpenShift());
        
        btnSelectEmployees.setOnClickListener(v -> showSelectEmployeeDialog());
        
        tvActiveEmployees.setOnClickListener(v -> showSelectEmployeeDialog());
        
        btnCloseShift.setOnClickListener(v -> showCloseShiftDialog());

        if (resId != -1) {
            loadUsers();
            loadShiftState();
        } else {
            Toast.makeText(this, "Chưa xác định được nhà hàng!", Toast.LENGTH_SHORT).show();
            finish();
        }
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
        progressBar.setVisibility(View.VISIBLE);
        layoutOpenShift.setVisibility(View.GONE);
        layoutActiveShift.setVisibility(View.GONE);

        apiService.getActiveShift(resId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> shift = response.body();
                    isShiftOpen = true;
                    
                    if (shift.get("id") != null) {
                        currentShiftId = ((Number) shift.get("id")).intValue();
                    }
                    if (shift.get("startingFund") != null) {
                        currentFund = ((Number) shift.get("startingFund")).doubleValue();
                    }
                    if (shift.get("employeeNames") != null) {
                        selectedEmployeeNames = (String) shift.get("employeeNames");
                    }
                    if (shift.get("employeeIds") != null) {
                        selectedEmployeeIds = (String) shift.get("employeeIds");
                    }
                    if (shift.get("totalRevenue") != null) {
                        currentTotalRevenue = ((Number) shift.get("totalRevenue")).doubleValue();
                    }
                    
                    // Parse date
                    startTimeStr = parseDate(shift.get("startTime"));

                    updateUI();
                } else {
                    isShiftOpen = false;
                    updateUI();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(QuanLyCaActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                isShiftOpen = false;
                updateUI();
            }
        });
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

        double fund;
        try {
            fund = Double.parseDouble(fundStr);
        } catch (Exception e) {
            Toast.makeText(this, "Tiền quỹ không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("restaurantId", resId);
        requestData.put("startingFund", fund);
        requestData.put("employeeNames", selectedEmployeeNames);
        requestData.put("employeeIds", selectedEmployeeIds);

        apiService.openShift(requestData).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(QuanLyCaActivity.this, "Mở ca làm việc thành công!", Toast.LENGTH_SHORT).show();
                    loadShiftState(); // Reload to get actual data
                } else {
                    Toast.makeText(QuanLyCaActivity.this, "Có lỗi xảy ra hoặc đã có ca đang mở", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(QuanLyCaActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCloseShiftDialog() {
        // Tính tổng doanh thu
        double totalRevenueToClose = currentTotalRevenue;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("XÁC NHẬN ĐÓNG CA LÀM");
        
        String closeTimeStr = new SimpleDateFormat("HH:mm (dd/MM/yyyy)", Locale.getDefault()).format(new java.util.Date());
        
        String msg = "Thời gian mở: " + startTimeStr + "\n"
                + "Thời gian đóng: " + closeTimeStr + "\n\n"
                + "Quỹ đầu ca: " + formatter.format(currentFund) + "đ\n"
                + "TỔNG DOANH THU TRONG CA: " + formatter.format(totalRevenueToClose) + "đ\n\n"
                + "Bạn có chắc chắn muốn kết thúc phiên làm việc này không?";
                
        builder.setMessage(msg);
        
        builder.setPositiveButton("ĐÓNG CA", (dialog, which) -> {
            closeShiftApi(totalRevenueToClose);
        });
        
        builder.setNegativeButton("HỦY", (dialog, which) -> dialog.dismiss());
        
        builder.show();
    }

    private void closeShiftApi(double totalRevenue) {
        if (currentShiftId == -1) return;
        
        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("totalRevenue", totalRevenue);

        apiService.closeShift(currentShiftId, requestData).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(QuanLyCaActivity.this, "Đã đóng ca làm việc", Toast.LENGTH_SHORT).show();
                    isShiftOpen = false;
                    currentFund = 0;
                    selectedEmployeeNames = "";
                    edtStartingFund.setText("");
                    tvSelectedEmployees.setText("");
                    currentShiftId = -1;
                    updateUI();
                } else {
                    Toast.makeText(QuanLyCaActivity.this, "Lỗi khi đóng ca", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(QuanLyCaActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (isShiftOpen) {
            layoutOpenShift.setVisibility(View.GONE);
            layoutActiveShift.setVisibility(View.VISIBLE);
            
            tvStartTime.setText(startTimeStr);
            tvStartingFund.setText(formatter.format(currentFund) + "đ");
            tvActiveEmployees.setText(selectedEmployeeNames);
            
            // Tạm thời chưa có API đếm số Hóa đơn, để ẩn
            tvOrderCount.setText("---");
            tvTotalRevenue.setText(formatter.format(currentTotalRevenue) + "đ");
        } else {
            layoutOpenShift.setVisibility(View.VISIBLE);
            layoutActiveShift.setVisibility(View.GONE);
            tvSelectedEmployees.setText(selectedEmployeeNames.isEmpty() ? "Chưa chọn nhân viên" : selectedEmployeeNames);
        }
    }

    private void loadUsers() {
        apiService.getUsersByRestaurant(resId).enqueue(new Callback<List<com.example.apporderfood.model.User>>() {
            @Override
            public void onResponse(Call<List<com.example.apporderfood.model.User>> call, Response<List<com.example.apporderfood.model.User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allUsers = response.body();
                    selectedUserItems = new boolean[allUsers.size()];
                }
            }

            @Override
            public void onFailure(Call<List<com.example.apporderfood.model.User>> call, Throwable t) {
                Toast.makeText(QuanLyCaActivity.this, "Không thể tải danh sách nhân viên", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSelectEmployeeDialog() {
        if (allUsers == null || allUsers.isEmpty()) {
            Toast.makeText(this, "Chưa có danh sách nhân viên", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] userNames = new String[allUsers.size()];
        for (int i = 0; i < allUsers.size(); i++) {
            userNames[i] = allUsers.get(i).getUsername();
            int userId = allUsers.get(i).getId();
            
            // Khôi phục trạng thái đã chọn từ selectedEmployeeIds
            selectedUserItems[i] = false;
            if (selectedEmployeeIds != null && !selectedEmployeeIds.isEmpty()) {
                String[] ids = selectedEmployeeIds.split(",");
                for (String idStr : ids) {
                    try {
                        if (Integer.parseInt(idStr.trim()) == userId) {
                            selectedUserItems[i] = true;
                            break;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn nhân viên ca này");
        builder.setMultiChoiceItems(userNames, selectedUserItems, (dialog, which, isChecked) -> {
            selectedUserItems[which] = isChecked;
        });

        builder.setPositiveButton("XÁC NHẬN", (dialog, which) -> {
            StringBuilder names = new StringBuilder();
            StringBuilder ids = new StringBuilder();
            
            for (int i = 0; i < selectedUserItems.length; i++) {
                if (selectedUserItems[i]) {
                    if (names.length() > 0) {
                        names.append(", ");
                        ids.append(",");
                    }
                    names.append(userNames[i]);
                    ids.append(allUsers.get(i).getId());
                }
            }
            
            selectedEmployeeNames = names.toString();
            selectedEmployeeIds = ids.toString();
            
            if (isShiftOpen && currentShiftId != -1) {
                // Đang trong ca -> gọi API cập nhật
                progressBar.setVisibility(View.VISIBLE);
                Map<String, Object> reqData = new HashMap<>();
                reqData.put("employeeNames", selectedEmployeeNames);
                reqData.put("employeeIds", selectedEmployeeIds);
                
                apiService.updateShiftEmployees(currentShiftId, reqData).enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            Toast.makeText(QuanLyCaActivity.this, "Đã cập nhật nhân sự!", Toast.LENGTH_SHORT).show();
                            updateUI();
                        } else {
                            Toast.makeText(QuanLyCaActivity.this, "Lỗi khi cập nhật nhân sự", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(QuanLyCaActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                updateUI();
            }
        });

        builder.setNegativeButton("HỦY", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private String parseDate(Object dateObj) {
        if (dateObj == null) return "N/A";
        if (dateObj instanceof String) {
            String createdAt = (String) dateObj;
            try {
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                java.util.Date date = parser.parse(createdAt);
                SimpleDateFormat printer = new SimpleDateFormat("HH:mm (dd/MM/yyyy)", Locale.getDefault());
                return printer.format(date);
            } catch (Exception e) {
                return createdAt;
            }
        } else if (dateObj instanceof List) {
            try {
                List<Number> list = (List<Number>) dateObj;
                if (list.size() >= 5) {
                    return String.format(Locale.getDefault(), "%02d:%02d (%02d/%02d/%04d)",
                            list.get(3).intValue(), list.get(4).intValue(),
                            list.get(2).intValue(), list.get(1).intValue(), list.get(0).intValue());
                }
            } catch (Exception e) {
                return "Lỗi ngày giờ";
            }
        }
        return "N/A";
    }
}
