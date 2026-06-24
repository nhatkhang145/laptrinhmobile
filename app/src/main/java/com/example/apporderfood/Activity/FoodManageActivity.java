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

    private void showAddFoodBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.activity_them_mon_an, null);
        bottomSheetDialog.setContentView(view);

        // Setup Spinner danh mục - lấy từ API thay vì hardcode
        Spinner spinnerCategory = view.findViewById(R.id.spinner_category);
        String[] categories = {"Mỳ Cay", "Lẩu", "Đồ Uống", "Ăn Vặt", "Tráng Miệng"};
        ArrayAdapter<String> adapterCategory = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(adapterCategory);

        view.findViewById(R.id.btn_close).setOnClickListener(v -> bottomSheetDialog.dismiss());
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> bottomSheetDialog.dismiss());
        view.findViewById(R.id.btn_save).setOnClickListener(v -> {
            // TODO: Gọi API POST /api/menu-items để thêm món mới
            bottomSheetDialog.dismiss();
            loadMenuFromApi(); // Reload lại danh sách
        });

        setupStatusToggle(view);
        bottomSheetDialog.show();
    }

    private void setupStatusToggle(View view) {
        View available = view.findViewById(R.id.tv_status_available);
        View soldOut   = view.findViewById(R.id.tv_status_sold_out);
        View paused    = view.findViewById(R.id.tv_status_paused);

        View.OnClickListener listener = v -> {
            available.setBackground(null);
            soldOut.setBackground(null);
            paused.setBackground(null);
            ((android.widget.TextView) available).setTextColor(getColor(R.color.text_secondary));
            ((android.widget.TextView) soldOut).setTextColor(getColor(R.color.text_secondary));
            ((android.widget.TextView) paused).setTextColor(getColor(R.color.text_secondary));

            v.setBackgroundResource(R.drawable.bg_tab_active_dark);
            ((android.widget.TextView) v).setTextColor(getColor(R.color.white));
        };

        available.setOnClickListener(listener);
        soldOut.setOnClickListener(listener);
        paused.setOnClickListener(listener);
    }
}
