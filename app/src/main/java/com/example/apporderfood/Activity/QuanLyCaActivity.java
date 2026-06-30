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
/**
 * Màn hình Quản lý ca làm việc.
 *
 * Chức năng:
 * - Kiểm tra ca làm việc hiện tại.
 * - Mở ca mới.
 * - Đóng ca làm việc.
 * - Chọn hoặc cập nhật nhân viên tham gia ca.
 * - Hiển thị thông tin ca đang hoạt động.
 */
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
    /**
     * Kiểm tra nhà hàng hiện có ca làm việc đang mở hay không.
     *
     * Nếu có:
     * - Lấy thông tin ca.
     * - Lưu dữ liệu vào các biến trạng thái.
     * - Cập nhật giao diện.
     *
     * Nếu không:
     * - Hiển thị giao diện mở ca.
     */
    private void loadShiftState() {
        progressBar.setVisibility(View.VISIBLE);
        layoutOpenShift.setVisibility(View.GONE);
        layoutActiveShift.setVisibility(View.GONE);

        apiService.getActiveShift(resId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                // Ẩn giao diện trong lúc tải dữ liệu
                progressBar.setVisibility(View.GONE);
                // Gọi API lấy ca đang hoạt động
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> shift = response.body();
                    // Có ca đang mở
                    isShiftOpen = true;
                    // Lưu thông tin ca hiện tại
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

                    // Chuyển thời gian mở ca sang định dạng hiển thị
                    startTimeStr = parseDate(shift.get("startTime"));
                    // Cập nhật giao diện theo trạng thái ca
                    updateUI();
                } else {
                    // Không có ca đang mở
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
    /**
     * Xử lý mở ca làm việc.
     *
     * Kiểm tra:
     * - Đã nhập tiền đầu ca.
     * - Đã chọn nhân viên.
     *
     * Sau đó gửi yêu cầu mở ca lên server.
     */
    private void handleOpenShift() {
        String fundStr = edtStartingFund.getText().toString().trim();
        // Kiểm tra tiền quỹ đầu ca
        if (fundStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiền quỹ đầu ca", Toast.LENGTH_SHORT).show();
            return;
        }
        // Kiểm tra đã chọn nhân viên chưa
        if (selectedEmployeeNames.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn nhân viên cho ca làm", Toast.LENGTH_SHORT).show();
            return;
        }
        // Tạo dữ liệu gửi lên API mở ca
        double fund;
        try {
            fund = Double.parseDouble(fundStr);
        } catch (Exception e) {
            Toast.makeText(this, "Tiền quỹ không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        // Ẩn giao diện trong lúc tải dữ liệu
        progressBar.setVisibility(View.VISIBLE);
        // Tạo dữ liệu gửi lên API mở ca
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("restaurantId", resId);
        requestData.put("startingFund", fund);
        requestData.put("employeeNames", selectedEmployeeNames);
        requestData.put("employeeIds", selectedEmployeeIds);
        // Gọi API mở ca
        apiService.openShift(requestData).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    // Mở ca thành công -> tải lại thông tin ca
                    Toast.makeText(QuanLyCaActivity.this, "Mở ca làm việc thành công!", Toast.LENGTH_SHORT).show();
                    loadShiftState();
                } else {
                    // Có lỗi xảy ra hoặc đã có ca đang mở
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
    /**
     * Hiển thị hộp thoại xác nhận đóng ca.
     *
     * Hiển thị:
     * - Thời gian mở.
     * - Thời gian đóng.
     * - Quỹ đầu ca.
     * - Tổng doanh thu.
     */
    private void showCloseShiftDialog() {
        // Lấy doanh thu hiện tại của ca
        double totalRevenueToClose = currentTotalRevenue;
        // Hiển thị thông tin trước khi xác nhận đóng ca
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
    /**
     * Gửi yêu cầu đóng ca làm việc.
     *
     * Sau khi đóng thành công:
     * - Xóa dữ liệu ca hiện tại.
     * - Hiển thị lại giao diện mở ca.
     */
    private void closeShiftApi(double totalRevenue) {
        if (currentShiftId == -1) return;
        
        progressBar.setVisibility(View.VISIBLE);
        // Gửi tổng doanh thu khi đóng ca
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("totalRevenue", totalRevenue);
        //Goị api đóng ca
        apiService.closeShift(currentShiftId, requestData).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    // Đóng ca thành công -> xóa dữ liệu ca hiện tại (set dữ liệu về mặc định).
                    Toast.makeText(QuanLyCaActivity.this, "Đã đóng ca làm việc", Toast.LENGTH_SHORT).show();
                    isShiftOpen = false;
                    currentFund = 0;
                    selectedEmployeeNames = "";
                    edtStartingFund.setText("");
                    tvSelectedEmployees.setText("");
                    currentShiftId = -1;
                    updateUI();
                } else {
                    //không thành công
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
    /**
     * Cập nhật giao diện theo trạng thái ca.
     *
     * Nếu ca đang mở:
     * - Hiển thị thông tin ca.
     *
     * Nếu chưa mở ca:
     * - Hiển thị màn hình mở ca.
     */
    private void updateUI() {
        // Hiển thị giao diện ca đang hoạt động
        if (isShiftOpen) {
            layoutOpenShift.setVisibility(View.GONE);
            layoutActiveShift.setVisibility(View.VISIBLE);
            
            tvStartTime.setText(startTimeStr);
            tvStartingFund.setText(formatter.format(currentFund) + "đ");
            tvActiveEmployees.setText(selectedEmployeeNames);
            
            // Tạm thời chưa có API đếm số Hóa đơn, để ẩn
            tvOrderCount.setText("---");
            tvTotalRevenue.setText(formatter.format(currentTotalRevenue) + "đ");
        }
        // Hiển thị giao diện mở ca
        else {
            layoutOpenShift.setVisibility(View.VISIBLE);
            layoutActiveShift.setVisibility(View.GONE);
            tvSelectedEmployees.setText(selectedEmployeeNames.isEmpty() ? "Chưa chọn nhân viên" : selectedEmployeeNames);
        }
    }
    /**
     * Tải danh sách nhân viên của nhà hàng.
     *
     * Danh sách này được sử dụng khi chọn
     * nhân viên tham gia ca làm việc.
     */
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
    /**
     * Hiển thị hộp thoại chọn nhân viên cho ca.
     *
     * Nếu ca đã mở:
     * - Cập nhật danh sách nhân viên của ca.
     *
     * Nếu chưa mở:
     * - Chỉ lưu danh sách để mở ca sau.
     */
    private void showSelectEmployeeDialog() {
        // Kiểm tra đã có danh sách nhân viên hay chưa
        if (allUsers == null || allUsers.isEmpty()) {
            Toast.makeText(this, "Chưa có danh sách nhân viên", Toast.LENGTH_SHORT).show();
            return;
        }
        //lấy user ra
        String[] userNames = new String[allUsers.size()];
        for (int i = 0; i < allUsers.size(); i++) {
            // Lấy tên và id của từng nhân viên
            userNames[i] = allUsers.get(i).getUsername();
            int userId = allUsers.get(i).getId();
            
            // Khôi phục trạng thái đã chọn từ selectedEmployeeIds
            selectedUserItems[i] = false;
            // Nếu ca đã có danh sách nhân viên thì khôi phục trạng thái đã chọn
            if (selectedEmployeeIds != null && !selectedEmployeeIds.isEmpty()) {
                // Tách chuỗi id nhân viên thành từng phần
                String[] ids = selectedEmployeeIds.split(",");
                for (String idStr : ids) {
                    try {
                        // Nếu id của nhân viên hiện tại nằm trong danh sách thì đánh dấu là đã được chọn

                        if (Integer.parseInt(idStr.trim()) == userId) {
                            selectedUserItems[i] = true;
                            break;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        // Tạo hộp thoại chọn nhiều nhân viên
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn nhân viên ca này");
        // Hiển thị danh sách nhân viên cùng trạng thái đã chọn
        builder.setMultiChoiceItems(userNames, selectedUserItems, (dialog, which, isChecked) -> {
            selectedUserItems[which] = isChecked;
        });
        // Xử lý khi người dùng xác nhận danh sách nhân viên
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
    /**
     * Chuyển dữ liệu thời gian từ API
     * sang định dạng HH:mm (dd/MM/yyyy)
     * để hiển thị trên giao diện.
     */
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
