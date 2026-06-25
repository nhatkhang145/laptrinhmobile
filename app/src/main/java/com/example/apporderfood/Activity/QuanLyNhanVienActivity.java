package com.example.apporderfood.Activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    private StaffAdapter staffAdapter;
    private List<User> staffList;
    private ZappyApiService apiService;


    // Giả sử lấy resId từ SharedPreferences sau khi Login, tạm fix = 1 để test
    private int currentResId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_nhan_vien);

        initViews();
        setupRecyclerView();
        setupListeners();

        // Khởi tạo API và tải dữ liệu
        apiService = RetrofitClient.getApiService();
        loadStaffData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);

        // Tạm thời bỏ qua ánh xạ cứng các số liệu online/offline vì Backend chưa hỗ trợ trường này
        rvStaff = findViewById(R.id.rvStaff);
        btnAddStaff = findViewById(R.id.btnAddStaff);

        etSearch = findViewById(R.id.etSearch);

        tvTotalStaff = findViewById(R.id.tvTotalStaff);
        tvOnlineStaff = findViewById(R.id.tvOnlineStaff);
        tvOfflineStaff = findViewById(R.id.tvOfflineStaff);
    }

    private void setupRecyclerView() {
        staffList = new ArrayList<>();
        staffAdapter = new StaffAdapter(this, staffList);
        rvStaff.setLayoutManager(new LinearLayoutManager(this));
        rvStaff.setAdapter(staffAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        if(etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    staffAdapter.filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Mở màn hình Thêm Nhân Viên
        btnAddStaff.setOnClickListener(v -> {
            // TODO: Bổ sung Intent nhảy sang ThemNhanVienActivity
            Toast.makeText(this, "Chuyển sang Thêm nhân viên", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadStaffData() {
        apiService.getUsersByRestaurant(currentResId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body();
                    staffAdapter.updateData(users);

                    int total = users.size();
                    tvTotalStaff.setText(String.valueOf(total));
                    // Giả lập UI: Tạm thời cho tất cả đều đang Online (Do DB chưa có trường status)
                    tvOnlineStaff.setText(String.valueOf(total));
                    tvOfflineStaff.setText("0");
                    // Hiện tại Backend không lưu trữ trạng thái Online/Offline

                } else {
                    Toast.makeText(QuanLyNhanVienActivity.this, "Lỗi lấy danh sách", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(QuanLyNhanVienActivity.this, "Mất kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}