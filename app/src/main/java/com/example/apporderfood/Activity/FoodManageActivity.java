package com.example.apporderfood.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apporderfood.R;
import com.example.apporderfood.adapter.FoodManageAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class FoodManageActivity extends AppCompatActivity {

    private RecyclerView rvFoodList;
    private FoodManageAdapter adapter;
    private FloatingActionButton fabAddFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_mon_an);

        rvFoodList = findViewById(R.id.rvFoodList);
        fabAddFood = findViewById(R.id.fab_add_food);
        
        rvFoodList.setLayoutManager(new LinearLayoutManager(this));

        List<FoodManageAdapter.FoodItem> foodList = new ArrayList<>();
        foodList.add(new FoodManageAdapter.FoodItem("Mì cay Hải Sản - Cấp 7", "Mì cay", "Bán chạy", "55.000đ", "CÒN MÓN"));
        foodList.add(new FoodManageAdapter.FoodItem("Mì cay Bò Mỹ", "Mì cay", "Phổ biến", "49.000đ", "CÒN MÓN"));
        foodList.add(new FoodManageAdapter.FoodItem("Mì cay Thập Cẩm", "Mì cay", "Yêu thích", "59.000đ", "HẾT MÓN"));
        foodList.add(new FoodManageAdapter.FoodItem("Mì cay Đùi Gà", "Mì cay", "Mới", "45.000đ", "TẠM NGỪNG"));
        foodList.add(new FoodManageAdapter.FoodItem("Mì cay Bạch Tuộc", "Mì cay", "Phổ biến", "55.000đ", "CÒN MÓN"));
        foodList.add(new FoodManageAdapter.FoodItem("Mì cay Cá Viên", "Mì cay", "Giá rẻ", "39.000đ", "CÒN MÓN"));

        adapter = new FoodManageAdapter(foodList);
        rvFoodList.setAdapter(adapter);

        fabAddFood.setOnClickListener(v -> showAddFoodBottomSheet());
    }

    private void showAddFoodBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.activity_them_mon_an, null);
        bottomSheetDialog.setContentView(view);

        // Setup Spinner
        Spinner spinnerCategory = view.findViewById(R.id.spinner_category);
        String[] categories = {"Mì cay", "Lẩu", "Đồ uống", "Tráng miệng"};
        ArrayAdapter<String> adapterCategory = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(adapterCategory);

        // Handle buttons
        view.findViewById(R.id.btn_close).setOnClickListener(v -> bottomSheetDialog.dismiss());
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> bottomSheetDialog.dismiss());
        view.findViewById(R.id.btn_save).setOnClickListener(v -> {
            // Process saving here
            bottomSheetDialog.dismiss();
        });

        // Status toggle logic (simplified)
        setupStatusToggle(view);

        bottomSheetDialog.show();
    }

    private void setupStatusToggle(View view) {
        View available = view.findViewById(R.id.tv_status_available);
        View soldOut = view.findViewById(R.id.tv_status_sold_out);
        View paused = view.findViewById(R.id.tv_status_paused);

        View.OnClickListener listener = v -> {
            available.setBackground(null);
            soldOut.setBackground(null);
            paused.setBackground(null);
            ((android.widget.TextView)available).setTextColor(getColor(R.color.text_secondary));
            ((android.widget.TextView)soldOut).setTextColor(getColor(R.color.text_secondary));
            ((android.widget.TextView)paused).setTextColor(getColor(R.color.text_secondary));

            v.setBackgroundResource(R.drawable.bg_tab_active_dark);
            ((android.widget.TextView)v).setTextColor(getColor(R.color.white));
        };

        available.setOnClickListener(listener);
        soldOut.setOnClickListener(listener);
        paused.setOnClickListener(listener);
    }
}
