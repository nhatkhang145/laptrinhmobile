package com.example.apporderfood.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.R;
import com.example.apporderfood.adapter.CategoryManageAdapter;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.Category;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryManageActivity extends AppCompatActivity implements CategoryManageAdapter.OnCategoryItemClickListener {

    private RecyclerView rvCategoryList;
    private CategoryManageAdapter adapter;
    private FloatingActionButton fabAddCategory;
    private ProgressBar pbLoading;
    private TextView tvEmptyState;
    private TextView tvTotalCategories, tvActiveCategories, tvHiddenCategories;

    private int resId = -1;

    private final ActivityResultLauncher<Intent> addCategoryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadCategories();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_danh_muc);

        // Lấy resId của nhà hàng hiện tại từ session đăng nhập
        SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        resId = prefs.getInt("RES_ID", -1);

        initViews();
        setupRecyclerView();
        loadCategories();
        setupListeners();
        setupBottomNav();
    }

    private void initViews() {
        rvCategoryList = findViewById(R.id.rvCategoryList);
        fabAddCategory = findViewById(R.id.fab_add_category);
        pbLoading = findViewById(R.id.pbCategoryLoading);
        tvEmptyState = findViewById(R.id.tvCategoryEmptyState);
        
        tvTotalCategories = findViewById(R.id.tvTotalCategories);
        tvActiveCategories = findViewById(R.id.tvActiveCategories);
        tvHiddenCategories = findViewById(R.id.tvHiddenCategories);
    }

    private void setupRecyclerView() {
        if (rvCategoryList != null) {
            rvCategoryList.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    private void loadCategories() {
        if (resId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin nhà hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (rvCategoryList != null) rvCategoryList.setVisibility(View.GONE);
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);

        ZappyApiService api = RetrofitClient.getApiService();
        api.getCategories(resId).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categoryList = response.body();
                    if (categoryList.isEmpty()) {
                        if (tvEmptyState != null) tvEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
                        adapter = new CategoryManageAdapter(categoryList, CategoryManageActivity.this);
                        if (rvCategoryList != null) {
                            rvCategoryList.setVisibility(View.VISIBLE);
                            rvCategoryList.setAdapter(adapter);
                        }
                        updateStats(categoryList);
                    }
                } else {
                    Toast.makeText(CategoryManageActivity.this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                Toast.makeText(CategoryManageActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStats(List<Category> list) {
        int total = list.size();
        int active = 0;
        int hidden = 0;
        for (Category item : list) {
            if (item.getStatus() != null && item.getStatus() == 1) active++;
            else hidden++;
        }
        if (tvTotalCategories != null) tvTotalCategories.setText(String.valueOf(total));
        if (tvActiveCategories != null) tvActiveCategories.setText(String.valueOf(active));
        if (tvHiddenCategories != null) tvHiddenCategories.setText(String.format(Locale.getDefault(), "%02d", hidden));
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        
        if (fabAddCategory != null) {
            fabAddCategory.setOnClickListener(v -> {
                Intent intent = new Intent(this, ThemDanhMucActivity.class);
                addCategoryLauncher.launch(intent);
            });
        }
    }

    @Override
    public void onEditClick(Category item) {
        Intent intent = new Intent(this, ThemDanhMucActivity.class);
        intent.putExtra("CATEGORY_DATA", item);
        addCategoryLauncher.launch(intent);
    }

    @Override
    public void onMoreClick(Category item, View view) {
        Toast.makeText(this, "Tùy chọn cho: " + item.getCatName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(Category item) {
        Toast.makeText(this, "Chi tiết: " + item.getCatName(), Toast.LENGTH_SHORT).show();
    }

    private void setupBottomNav() {
        View navOrder = findViewById(R.id.navOrder);
        View navSoDo = findViewById(R.id.navSoDo);
        View navTienIch = findViewById(R.id.navTienIch);

        if (navOrder != null) {
            navOrder.setOnClickListener(v -> {
                startActivity(new Intent(this, DanhSachOrderActivity.class));
                overridePendingTransition(0, 0);
            });
        }
        if (navSoDo != null) {
            navSoDo.setOnClickListener(v -> {
                startActivity(new Intent(this, SoDobanActivity.class));
                overridePendingTransition(0, 0);
            });
        }
        if (navTienIch != null) {
            navTienIch.setOnClickListener(v -> {
                startActivity(new Intent(this, TienIchActivity.class));
                overridePendingTransition(0, 0);
                finish();
            });
        }
    }
}
