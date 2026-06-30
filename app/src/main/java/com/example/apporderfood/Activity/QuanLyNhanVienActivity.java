package com.example.apporderfood.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.apporderfood.R;
import com.example.apporderfood.adapter.StaffAdapter;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.User;
import com.google.android.material.button.MaterialButton;
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuanLyNhanVienActivity extends AppCompatActivity {

    private IconicsImageView btnBack;
    private EditText etSearch;
    private TextView tvTotalStaff,tvOnlineStaff, tvOfflineStaff;;
    private MaterialButton btnAddStaff;
    private RecyclerView rvStaff;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tabAll, tabManager, tabCashier,tabStaff;
    private TextView currentSelectedTab;

    private StaffAdapter staffAdapter;
    private List<User> staffList;
    private ZappyApiService apiService;
    private LinearLayout navOrder;
    private LinearLayout navSoDo;
    private LinearLayout navTienIch;
    // Dùng để tránh gọi loadStaffData() 2 lần khi Activity vừa mới mở
    private boolean isFirstLoad = true;
    private int currentResId ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quan_ly_nhan_vien);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        initViews();
        setupRecyclerView();
        setupListeners();

        SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        currentResId = prefs.getInt("RES_ID", -1);
        if (currentResId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin nhà hàng. Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();

            // Thoát ra màn hình đăng nhập
            Intent intent = new Intent(this, DangNhapActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Khởi tạo API và tải dữ liệu
        apiService = RetrofitClient.getApiService();
        loadStaffData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Bỏ qua lần đầu vì onCreate() đã load rồi, tránh gọi API trùng 2 lần
        if (isFirstLoad) {
            isFirstLoad = false;
            return;
        }

        // Mỗi khi Activity hiện lại (ví dụ quay về từ màn hình Thêm nhân viên)
        // sẽ tự động tải lại danh sách mới nhất
        loadStaffData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);

        rvStaff = findViewById(R.id.rvStaff);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        btnAddStaff = findViewById(R.id.btnAddStaff);

        etSearch = findViewById(R.id.etSearch);

        tvTotalStaff = findViewById(R.id.tvTotalStaff);
        tvOnlineStaff = findViewById(R.id.tvOnlineStaff);
        tvOfflineStaff = findViewById(R.id.tvOfflineStaff);

        tabAll = findViewById(R.id.tabAll);

        tabManager = findViewById(R.id.tabManager);
        tabCashier = findViewById(R.id.tabCashier);
        tabStaff= findViewById(R.id.tabStaff);

        navOrder=findViewById(R.id.navOrder);
        navSoDo=findViewById(R.id.navSoDo);
        navTienIch=findViewById(R.id.navTienIch);



        currentSelectedTab = tabAll; // Mặc định chọn tab "Tất cả"
    }

    private void setupRecyclerView() {
        staffList = new ArrayList<>();
        staffAdapter = new StaffAdapter(this, staffList);
        rvStaff.setLayoutManager(new LinearLayoutManager(this));
        rvStaff.setAdapter(staffAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Kéo từ trên xuống để tải lại danh sách
        swipeRefresh.setOnRefreshListener(this::loadStaffData);

        if(etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    staffAdapter.filterByText(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}

            });
            // Role = null (Tất cả), Role = 1 (Quản lý), Role = 0 (Nhân viên), Role=2(Thu ngân)
            tabAll.setOnClickListener(v -> selectTab(tabAll, null));
            tabManager.setOnClickListener(v -> selectTab(tabManager, 1));


            tabCashier.setOnClickListener(v -> selectTab(tabCashier, 2));

            tabStaff.setOnClickListener(v -> selectTab(tabStaff, 0));

            navOrder.setOnClickListener(v -> {
                startActivity(new Intent(this, DanhSachOrderActivity.class));
                overridePendingTransition(0, 0);
            });

            navSoDo.setOnClickListener(v -> {
                startActivity(new Intent(this, SoDobanActivity.class));
                overridePendingTransition(0, 0);
            });

            navTienIch.setOnClickListener(v -> { });
        }

        // Mở màn hình Thêm Nhân Viên
        btnAddStaff.setOnClickListener(v -> {
            Intent intent = new Intent(QuanLyNhanVienActivity.this, ThemNhanVienActivity.class);
            startActivity(intent);
        });
    }

    private void loadStaffData() {
        apiService.getUsersByRestaurant(currentResId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body();
                    staffAdapter.updateData(users);
                    calculateStats(users);

                } else {
                    Toast.makeText(QuanLyNhanVienActivity.this, "Lỗi lấy danh sách", Toast.LENGTH_SHORT).show();
                }

                if (swipeRefresh.isRefreshing()) {
                    swipeRefresh.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(QuanLyNhanVienActivity.this, "Mất kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                if (swipeRefresh.isRefreshing()) {
                    swipeRefresh.setRefreshing(false);
                }
            }
        });
    }
    private void calculateStats(List<User> users) {
        int total = 0;
        int online = 0;
        int offline = 0;

        for (User user : users) {
            // Chỉ thống kê nhân viên còn hoạt động
            if (!Boolean.TRUE.equals(user.getIsActive())) {
                continue;
            }
            total++;
            if (Boolean.TRUE.equals(user.getIsOnline())) {
                online++;
            } else {
                offline++;
            }
        }

        tvTotalStaff.setText(String.valueOf(total));
        tvOnlineStaff.setText(String.valueOf(online));
        tvOfflineStaff.setText(String.valueOf(offline));
    }
    private void selectTab(TextView selectedTab, Integer roleId) {
        if (currentSelectedTab == selectedTab) return;

        // Reset tab cũ
        currentSelectedTab.setBackgroundResource(R.drawable.bg_input_selector);
        currentSelectedTab.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
        currentSelectedTab.setTypeface(null, android.graphics.Typeface.NORMAL);

        // Đổi màu tab mới
        selectedTab.setBackgroundResource(R.drawable.bg_tab_selected_dark);
        selectedTab.setTextColor(getResources().getColor(R.color.white, getTheme()));
        selectedTab.setTypeface(null, android.graphics.Typeface.BOLD);

        currentSelectedTab = selectedTab;

        // Gọi Adapter để lọc dữ liệu
        staffAdapter.filterByRole(roleId);
    }
}