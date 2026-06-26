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
import com.example.apporderfood.model.Category;
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
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        rvCategoryList.setVisibility(View.GONE);

        // Giả lập delay tải dữ liệu
        new android.os.Handler().postDelayed(() -> {
            List<Category> categoryList = new ArrayList<>();
            // Category(Integer id, String catName, String description, String imageUrl, Integer status, Integer itemCount)
            categoryList.add(new Category(1, "Burger", "Các loại burger bò, gà...", null, 1, 45));
            categoryList.add(new Category(2, "Pizza", "Pizza truyền thống Ý", null, 1, 32));
            categoryList.add(new Category(3, "Đồ uống", "Nước ngọt, cà phê, trà", null, 1, 58));
            categoryList.add(new Category(4, "Tráng miệng", "Kem, bánh ngọt, chè", null, 0, 12));
            categoryList.add(new Category(5, "Combo", "Tiết kiệm cho nhóm", null, 1, 8));
            categoryList.add(new Category(6, "Món ăn nhanh", "Khoai tây chiên, snack", null, 0, 24));

            if (pbLoading != null) pbLoading.setVisibility(View.GONE);
            if (categoryList.isEmpty()) {
                if (tvEmptyState != null) tvEmptyState.setVisibility(View.VISIBLE);
                rvCategoryList.setVisibility(View.GONE);
            } else {
                if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
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
    public void onEditClick(Category item) {
        // Logic chỉnh sửa: Truyền object sang màn hình thêm (chế độ sửa)
        Intent intent = new Intent(this, ThemDanhMucActivity.class);
        intent.putExtra("CATEGORY_DATA", item);
        addCategoryLauncher.launch(intent);
    }

    @Override
    public void onMoreClick(Category item, View view) {
        Toast.makeText(this, "Tùy chọn: " + item.getCatName(), Toast.LENGTH_SHORT).show();
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
