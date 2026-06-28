package com.example.apporderfood.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.R;
import com.example.apporderfood.adapter.FoodInCategoryAdapter;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.Category;
import com.example.apporderfood.model.MenuItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChiTietDanhMucActivity extends AppCompatActivity {

    private TextView tvCategoryName, tvCategoryDesc, tvCategoryStatus;
    private TextView tvTotalFoods, tvAvailableFoods, tvOutOfStockFoods;
    private RecyclerView rvFoodsInCategory;
    private ProgressBar pbLoading;
    private View llEmptyState;

    private FoodInCategoryAdapter adapter;
    private List<MenuItem> foodList = new ArrayList<>();
    private Category category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_danh_muc);

        // Nhận Category từ Intent
        category = (Category) getIntent().getSerializableExtra("CATEGORY_DATA");
        if (category == null) {
            Toast.makeText(this, "Không tìm thấy danh mục!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        bindCategoryInfo();
        setupRecyclerView();
        loadFoods();
    }

    private void initViews() {
        tvCategoryName   = findViewById(R.id.tvCategoryName);
        tvCategoryDesc   = findViewById(R.id.tvCategoryDesc);
        tvCategoryStatus = findViewById(R.id.tvCategoryStatus);
        tvTotalFoods     = findViewById(R.id.tvTotalFoods);
        tvAvailableFoods = findViewById(R.id.tvAvailableFoods);
        tvOutOfStockFoods = findViewById(R.id.tvOutOfStockFoods);
        rvFoodsInCategory = findViewById(R.id.rvFoodsInCategory);
        pbLoading         = findViewById(R.id.pbLoading);
        llEmptyState      = findViewById(R.id.llEmptyState);

        // Toolbar title
        TextView tvTitle = findViewById(R.id.tvToolbarTitle);
        if (tvTitle != null) tvTitle.setText("Chi tiết danh mục");

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void bindCategoryInfo() {
        tvCategoryName.setText(category.getCatName());
        String desc = category.getDescription();
        if (desc != null && !desc.isEmpty()) {
            tvCategoryDesc.setText(desc);
            tvCategoryDesc.setVisibility(View.VISIBLE);
        } else {
            tvCategoryDesc.setVisibility(View.GONE);
        }

        boolean active = category.getStatus() == null || category.getStatus() == 1;
        if (active) {
            tvCategoryStatus.setText("HOẠT ĐỘNG");
            tvCategoryStatus.setBackgroundResource(R.drawable.bg_status_available);
            tvCategoryStatus.setTextColor(getColor(android.R.color.holo_green_dark));
        } else {
            tvCategoryStatus.setText("TẠM ẨN");
            tvCategoryStatus.setBackgroundResource(R.drawable.bg_status_paused);
            tvCategoryStatus.setTextColor(getColor(android.R.color.holo_orange_dark));
        }
    }

    private void setupRecyclerView() {
        adapter = new FoodInCategoryAdapter(foodList, item -> confirmRemove(item));
        rvFoodsInCategory.setLayoutManager(new LinearLayoutManager(this));
        rvFoodsInCategory.setAdapter(adapter);
    }

    private void loadFoods() {
        if (category.getId() == null) return;
        pbLoading.setVisibility(View.VISIBLE);
        rvFoodsInCategory.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.GONE);

        ZappyApiService api = RetrofitClient.getApiService();
        api.getMenuByCategory(category.getId()).enqueue(new Callback<List<MenuItem>>() {
            @Override
            public void onResponse(Call<List<MenuItem>> call, Response<List<MenuItem>> response) {
                pbLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    foodList.clear();
                    foodList.addAll(response.body());
                    adapter.setItems(foodList);
                    updateStats();
                    if (foodList.isEmpty()) {
                        llEmptyState.setVisibility(View.VISIBLE);
                        rvFoodsInCategory.setVisibility(View.GONE);
                    } else {
                        rvFoodsInCategory.setVisibility(View.VISIBLE);
                        llEmptyState.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(ChiTietDanhMucActivity.this,
                            "Lỗi tải danh sách món: " + response.code(), Toast.LENGTH_SHORT).show();
                    llEmptyState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<MenuItem>> call, Throwable t) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(ChiTietDanhMucActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                llEmptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateStats() {
        int total = foodList.size();
        int available = 0;
        for (MenuItem item : foodList) {
            if (item.getIsAvailable() == null || item.getIsAvailable()) available++;
        }
        int outOfStock = total - available;
        tvTotalFoods.setText(String.valueOf(total));
        tvAvailableFoods.setText(String.valueOf(available));
        tvOutOfStockFoods.setText(String.valueOf(outOfStock));
    }

    /**
     * Hiển thị dialog xác nhận trước khi xóa món khỏi danh mục.
     * Xóa = đặt catId = null thông qua updateMenuItem.
     */
    private void confirmRemove(MenuItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa khỏi danh mục")
                .setMessage("Bạn có chắc muốn xóa \"" + item.getItemName()
                        + "\" khỏi danh mục \"" + category.getCatName() + "\"?\n"
                        + "Món ăn vẫn tồn tại trong hệ thống, chỉ bị bỏ khỏi danh mục này.")
                .setPositiveButton("Xóa", (dialog, which) -> removeFoodFromCategory(item))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void removeFoodFromCategory(MenuItem item) {
        ZappyApiService api = RetrofitClient.getApiService();
        // Gửi catId = null để bỏ danh mục
        Map<String, Object> data = new HashMap<>();
        data.put("catId", null);
        data.put("itemName", item.getItemName());
        if (item.getPrice() != null) data.put("price", item.getPrice().doubleValue());
        if (item.getIsAvailable() != null) data.put("isAvailable", item.getIsAvailable());
        if (item.getUnit() != null) data.put("unitId", item.getUnit().getId());

        api.updateMenuItem(item.getId(), data).enqueue(new Callback<MenuItem>() {
            @Override
            public void onResponse(Call<MenuItem> call, Response<MenuItem> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ChiTietDanhMucActivity.this,
                            "Đã xóa \"" + item.getItemName() + "\" khỏi danh mục",
                            Toast.LENGTH_SHORT).show();
                    // Xóa khỏi list local và cập nhật UI
                    foodList.remove(item);
                    adapter.setItems(new ArrayList<>(foodList));
                    updateStats();
                    if (foodList.isEmpty()) {
                        llEmptyState.setVisibility(View.VISIBLE);
                        rvFoodsInCategory.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(ChiTietDanhMucActivity.this,
                            "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MenuItem> call, Throwable t) {
                Toast.makeText(ChiTietDanhMucActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
