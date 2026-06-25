package com.example.apporderfood.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.R;
import com.example.apporderfood.adapter.FoodManageAdapter;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.MenuItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodManageActivity extends AppCompatActivity {

    private RecyclerView rvFoodList;
    private FoodManageAdapter adapter;
    private FloatingActionButton fabAddFood;
    private ProgressBar progressBar;

    private int resId = -1;

    private List<com.example.apporderfood.model.Category> currentCategories = new ArrayList<>();
    private List<com.example.apporderfood.model.Unit> currentUnits = new ArrayList<>();
    private boolean isAvailable = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_mon_an);

        // Lấy resId từ SharedPreferences (đã lưu sau khi đăng nhập)
        SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        resId = prefs.getInt("RES_ID", -1);

        rvFoodList  = findViewById(R.id.rvFoodList);
        fabAddFood  = findViewById(R.id.fab_add_food);

        rvFoodList.setLayoutManager(new LinearLayoutManager(this));

        loadMenuFromApi();

        fabAddFood.setOnClickListener(v -> showAddFoodBottomSheet());
    }

    /**
     * Gọi API GET /api/menu-items/restaurant/{resId}
     * Lấy danh sách món ăn thực tế từ database
     */
    private void loadMenuFromApi() {
        if (resId == -1) {
            Toast.makeText(this, "Không xác định được nhà hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        ZappyApiService api = RetrofitClient.getApiService();
        api.getMenuByRestaurant(resId).enqueue(new Callback<List<MenuItem>>() {
            @Override
            public void onResponse(Call<List<MenuItem>> call, Response<List<MenuItem>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<MenuItem> menuItems = response.body();
                    List<FoodManageAdapter.FoodItem> foodList = new ArrayList<>();

                    NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));

                    for (MenuItem item : menuItems) {
                        String tenMon   = item.getItemName();
                        String danhMuc  = (item.getCategory() != null) ? item.getCategory().getCatName() : "—";
                        String donVi    = (item.getUnit() != null)     ? item.getUnit().getUnitName()    : "";
                        String giaHien  = fmt.format(item.getPrice()) + "đ";

                        // Trạng thái mặc định "CÒN MÓN" – sau này có thể thêm trường status
                        foodList.add(new FoodManageAdapter.FoodItem(
                                tenMon, danhMuc, donVi, giaHien, "CÒN MÓN"
                        ));
                    }

                    adapter = new FoodManageAdapter(foodList);
                    rvFoodList.setAdapter(adapter);

                    if (menuItems.isEmpty()) {
                        Toast.makeText(FoodManageActivity.this,
                                "Chưa có món nào. Thêm món mới nhé!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(FoodManageActivity.this,
                            "Không thể tải danh sách món!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MenuItem>> call, Throwable t) {
                Toast.makeText(FoodManageActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void loadCategoriesAndUnits(Spinner spinnerCategory) {
        ZappyApiService api = RetrofitClient.getApiService();
        api.getCategories(resId).enqueue(new Callback<List<com.example.apporderfood.model.Category>>() {
            @Override
            public void onResponse(Call<List<com.example.apporderfood.model.Category>> call, Response<List<com.example.apporderfood.model.Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentCategories = response.body();
                    List<String> catNames = new ArrayList<>();
                    for (com.example.apporderfood.model.Category c : currentCategories) {
                        catNames.add(c.getCatName());
                    }
                    ArrayAdapter<String> adapterCategory = new ArrayAdapter<>(FoodManageActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, catNames);
                    spinnerCategory.setAdapter(adapterCategory);
                }
            }
            @Override
            public void onFailure(Call<List<com.example.apporderfood.model.Category>> call, Throwable t) {}
        });

        api.getUnits(resId).enqueue(new Callback<List<com.example.apporderfood.model.Unit>>() {
            @Override
            public void onResponse(Call<List<com.example.apporderfood.model.Unit>> call, Response<List<com.example.apporderfood.model.Unit>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUnits = response.body();
                }
            }
            @Override
            public void onFailure(Call<List<com.example.apporderfood.model.Unit>> call, Throwable t) {}
        });
    }

    private void showAddFoodBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.activity_them_mon_an, null);
        bottomSheetDialog.setContentView(view);

        Spinner spinnerCategory = view.findViewById(R.id.spinner_category);
        loadCategoriesAndUnits(spinnerCategory);

        android.widget.EditText edtFoodName = view.findViewById(R.id.edt_food_name);
        android.widget.EditText edtPrice = view.findViewById(R.id.edt_price);

        view.findViewById(R.id.btn_close).setOnClickListener(v -> bottomSheetDialog.dismiss());
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> bottomSheetDialog.dismiss());
        
        setupStatusToggle(view);

        view.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String name = edtFoodName.getText().toString().trim();
            String priceStr = edtPrice.getText().toString().trim();
            
            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên và giá món ăn", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (currentCategories.isEmpty() || currentUnits.isEmpty()) {
                Toast.makeText(this, "Chưa có danh mục hoặc đơn vị tính. Vui lòng tạo trước!", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedCatPos = spinnerCategory.getSelectedItemPosition();
            if (selectedCatPos < 0) return;
            
            Integer catId = currentCategories.get(selectedCatPos).getId();
            Integer unitId = currentUnits.get(0).getId(); // Mặc định lấy đơn vị tính đầu tiên

            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("catId", catId);
            data.put("unitId", unitId);
            data.put("itemName", name);
            data.put("price", Double.parseDouble(priceStr));
            data.put("isAvailable", isAvailable);

            ZappyApiService api = RetrofitClient.getApiService();
            api.createMenuItem(data).enqueue(new Callback<MenuItem>() {
                @Override
                public void onResponse(Call<MenuItem> call, Response<MenuItem> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(FoodManageActivity.this, "Thêm món thành công!", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                        loadMenuFromApi();
                    } else {
                        Toast.makeText(FoodManageActivity.this, "Lỗi thêm món: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<MenuItem> call, Throwable t) {
                    Toast.makeText(FoodManageActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        bottomSheetDialog.show();
    }

    private void setupStatusToggle(View view) {
        View available = view.findViewById(R.id.tv_status_available);
        View soldOut   = view.findViewById(R.id.tv_status_sold_out);
        View paused    = view.findViewById(R.id.tv_status_paused);

        isAvailable = true;

        View.OnClickListener listener = v -> {
            available.setBackground(null);
            soldOut.setBackground(null);
            paused.setBackground(null);
            ((android.widget.TextView) available).setTextColor(getColor(R.color.text_secondary));
            ((android.widget.TextView) soldOut).setTextColor(getColor(R.color.text_secondary));
            ((android.widget.TextView) paused).setTextColor(getColor(R.color.text_secondary));

            v.setBackgroundResource(R.drawable.bg_tab_active_dark);
            ((android.widget.TextView) v).setTextColor(getColor(R.color.white));

            if (v.getId() == R.id.tv_status_available) {
                isAvailable = true;
            } else {
                isAvailable = false;
            }
        };

        available.setOnClickListener(listener);
        soldOut.setOnClickListener(listener);
        paused.setOnClickListener(listener);
    }
}
