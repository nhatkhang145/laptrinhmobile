package com.example.apporderfood.Activity;

import android.content.Intent;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CategoryManageActivity extends AppCompatActivity implements CategoryManageAdapter.OnCategoryItemClickListener {

    private RecyclerView rvCategoryList;
    private CategoryManageAdapter adapter;
    private FloatingActionButton fabAddCategory;
    private ProgressBar pbLoading;
    private TextView tvEmptyState;

    // ActivityResultLauncher to handle return from Add Category screen
    private final ActivityResultLauncher<Intent> addCategoryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Refresh data
                    loadCategories();
                    Toast.makeText(this, "Danh sách đã được cập nhật", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_danh_muc);

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
    }

    private void setupRecyclerView() {
        rvCategoryList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadCategories() {
        // Show loading
        pbLoading.setVisibility(View.VISIBLE);
        rvCategoryList.setVisibility(View.GONE);

        // Giả lập delay tải dữ liệu
        new android.os.Handler().postDelayed(() -> {
            List<CategoryManageAdapter.CategoryItem> categoryList = new ArrayList<>();
            categoryList.add(new CategoryManageAdapter.CategoryItem("Burger", "HOẠT ĐỘNG", 45, "Các loại burger bò, gà..."));
            categoryList.add(new CategoryManageAdapter.CategoryItem("Pizza", "HOẠT ĐỘNG", 32, "Pizza truyền thống Ý"));
            categoryList.add(new CategoryManageAdapter.CategoryItem("Đồ uống", "HOẠT ĐỘNG", 58, "Nước ngọt, cà phê, trà"));
            categoryList.add(new CategoryManageAdapter.CategoryItem("Tráng miệng", "TẠM ẨN", 12, "Kem, bánh ngọt, chè"));
            categoryList.add(new CategoryManageAdapter.CategoryItem("Combo", "HOẠT ĐỘNG", 8, "Tiết kiệm cho nhóm"));
            categoryList.add(new CategoryManageAdapter.CategoryItem("Món ăn nhanh", "TẠM ẨN", 24, "Khoai tây chiên, snack"));

            pbLoading.setVisibility(View.GONE);
            if (categoryList.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                rvCategoryList.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                rvCategoryList.setVisibility(View.VISIBLE);
                adapter = new CategoryManageAdapter(categoryList, this);
                rvCategoryList.setAdapter(adapter);
            }
        }, 1000);
    }

    private void setupListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        fabAddCategory.setOnClickListener(v -> {
            Intent intent = new Intent(this, ThemDanhMucActivity.class);
            addCategoryLauncher.launch(intent);
        });
    }

    @Override
    public void onEditClick(CategoryManageAdapter.CategoryItem item) {
        // Logic chỉnh sửa: Truyền object sang màn hình thêm (chế độ sửa)
        Intent intent = new Intent(this, ThemDanhMucActivity.class);
        intent.putExtra("IS_EDIT", true);
        intent.putExtra("CATEGORY_NAME", item.getName());
        intent.putExtra("CATEGORY_DESC", item.getDescription());
        addCategoryLauncher.launch(intent);
    }

    @Override
    public void onMoreClick(CategoryManageAdapter.CategoryItem item, View view) {
        Toast.makeText(this, "Tùy chọn: " + item.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(CategoryManageAdapter.CategoryItem item) {
        Toast.makeText(this, "Chi tiết: " + item.getName(), Toast.LENGTH_SHORT).show();
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
