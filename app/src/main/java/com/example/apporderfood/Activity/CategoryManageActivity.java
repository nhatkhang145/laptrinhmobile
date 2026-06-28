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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.R;
import com.example.apporderfood.adapter.CategoryManageAdapter;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.Category;
import com.example.apporderfood.model.MenuItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryManageActivity extends AppCompatActivity
        implements CategoryManageAdapter.OnCategoryItemClickListener {

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quan_ly_danh_muc);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        resId = prefs.getInt("RES_ID", -1);

        initViews();
        setupRecyclerView();
        loadCategories();
        setupListeners();
        setupBottomNav();
    }

    private void initViews() {
        rvCategoryList      = findViewById(R.id.rvCategoryList);
        fabAddCategory      = findViewById(R.id.fab_add_category);
        pbLoading           = findViewById(R.id.pbCategoryLoading);
        tvEmptyState        = findViewById(R.id.tvCategoryEmptyState);
        tvTotalCategories   = findViewById(R.id.tvTotalCategories);
        tvActiveCategories  = findViewById(R.id.tvActiveCategories);
        tvHiddenCategories  = findViewById(R.id.tvHiddenCategories);
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
                        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                        if (tvEmptyState != null) tvEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        // Gọi thêm API lấy menu để đếm số món cho mỗi danh mục
                        api.getMenuByRestaurant(resId, "").enqueue(new Callback<List<MenuItem>>() {
                            @Override
                            public void onResponse(Call<List<MenuItem>> call, Response<List<MenuItem>> responseMenu) {
                                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                                if (responseMenu.isSuccessful() && responseMenu.body() != null) {
                                    List<MenuItem> menuItems = responseMenu.body();
                                    for (Category cat : categoryList) {
                                        int count = 0;
                                        for (MenuItem item : menuItems) {
                                            if (item.getCategory() != null && item.getCategory().getId().equals(cat.getId())) {
                                                count++;
                                            }
                                        }
                                        cat.setItemCount(count);
                                    }
                                }
                                displayCategories(categoryList);
                            }

                            @Override
                            public void onFailure(Call<List<MenuItem>> call, Throwable t) {
                                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                                displayCategories(categoryList);
                            }
                        });
                    }
                } else {
                    if (pbLoading != null) pbLoading.setVisibility(View.GONE);
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

    private void displayCategories(List<Category> categoryList) {
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
        adapter = new CategoryManageAdapter(categoryList, CategoryManageActivity.this);
        if (rvCategoryList != null) {
            rvCategoryList.setVisibility(View.VISIBLE);
            rvCategoryList.setAdapter(adapter);
        }
        updateStats(categoryList);
    }

    private void updateStats(List<Category> list) {
        int total = list.size();
        int active = 0;
        int hidden = 0;
        for (Category item : list) {
            // ✅ Đồng nhất: null hoặc 1 = HOẠT ĐỘNG
            if (item.getStatus() == null || item.getStatus() == 1) active++;
            else hidden++;
        }
        if (tvTotalCategories != null) tvTotalCategories.setText(String.valueOf(total));
        if (tvActiveCategories != null) tvActiveCategories.setText(String.valueOf(active));
        if (tvHiddenCategories != null) tvHiddenCategories.setText(String.valueOf(hidden));
    }

    /** Cập nhật stats từ data local hiện tại của adapter (sau khi toggle status) */
    private void updateStatsFromAdapter() {
        if (adapter == null) return;
        // Lấy lại stats bằng cách đọc từ categoryList đang được giữ trong adapter
        // (adapter đã cập nhật status local rồi)
        loadCategories(); // Đơn giản nhất: reload lại để stats luôn chính xác
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (fabAddCategory != null) {
            fabAddCategory.setOnClickListener(v -> {
                Intent intent = new Intent(this, ThemDanhMucActivity.class);
                addCategoryLauncher.launch(intent);
            });
        }
    }

    // ── Callbacks từ adapter ────────────────────────────────────────────────

    @Override
    public void onEditClick(Category item) {
        Intent intent = new Intent(this, ThemDanhMucActivity.class);
        intent.putExtra("IS_EDIT", true);
        intent.putExtra("CATEGORY_DATA", item);
        addCategoryLauncher.launch(intent);
    }

    @Override
    public void onStatusToggleClick(Category item, int position) {
        if (item.getId() == null) return;

        // Đảo ngược trạng thái: null/1 → 0 (ẩn), 0 → 1 (hoạt động)
        boolean currentlyActive = item.getStatus() == null || item.getStatus() == 1;
        int newStatus = currentlyActive ? 0 : 1;
        String newStatusLabel = currentlyActive ? "TẠM ẨN" : "HOẠT ĐỘNG";

        ZappyApiService api = RetrofitClient.getApiService();
        Map<String, Object> body = new HashMap<>();
        body.put("resId", resId); // THÊM RES ID VÌ API YÊU CẦU
        body.put("catName", item.getCatName());
        body.put("status", newStatus);
        if (item.getDescription() != null) body.put("description", item.getDescription());

        api.updateCategory(item.getId(), body).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                if (response.isSuccessful()) {
                    // Cập nhật local ngay, không cần reload toàn bộ
                    item.setStatus(newStatus);
                    if (adapter != null) adapter.updateItemStatus(position, newStatus);
                    updateStatsFromAdapter();
                    Toast.makeText(CategoryManageActivity.this,
                            "\"" + item.getCatName() + "\" → " + newStatusLabel,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CategoryManageActivity.this,
                            "Cập nhật thất bại (lỗi " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                Toast.makeText(CategoryManageActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteClick(Category item) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa danh mục")
                .setMessage("Bạn có chắc muốn xóa danh mục \"" + item.getCatName() + "\"?\n"
                        + "Các món ăn trong danh mục này sẽ không còn được phân loại.")
                .setPositiveButton("Xóa", (dialog, which) -> performDelete(item))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performDelete(Category item) {
        if (item.getId() == null) return;

        ZappyApiService api = RetrofitClient.getApiService();
        api.deleteCategory(item.getId()).enqueue(new Callback<Map>() {
            @Override
            public void onResponse(Call<Map> call, Response<Map> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CategoryManageActivity.this,
                            "Đã xóa danh mục: " + item.getCatName(), Toast.LENGTH_SHORT).show();
                    // Xóa khỏi list ngay không cần gọi API lại
                    if (adapter != null) {
                        adapter.removeItem(item);
                        // Cập nhật stats từ adapter
                        loadCategories();
                    }
                } else if (response.code() == 409 || response.code() == 400) {
                    Toast.makeText(CategoryManageActivity.this,
                            "Không thể xóa: danh mục đang có món ăn hoặc đang được sử dụng",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(CategoryManageActivity.this,
                            "Xóa thất bại (lỗi " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map> call, Throwable t) {
                Toast.makeText(CategoryManageActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(Category item) {
        Intent intent = new Intent(this, ChiTietDanhMucActivity.class);
        intent.putExtra("CATEGORY_DATA", item);
        startActivity(intent);
    }

    // ── Bottom navigation ──────────────────────────────────────────────────

    private void setupBottomNav() {
        View navOrder  = findViewById(R.id.navOrder);
        View navSoDo   = findViewById(R.id.navSoDo);
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
