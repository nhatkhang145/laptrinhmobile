package com.example.apporderfood.Activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apporderfood.R;
import com.example.apporderfood.adapter.FoodManageAdapter;
import java.util.ArrayList;
import java.util.List;

public class FoodManageActivity extends AppCompatActivity {

    private RecyclerView rvFoodList;
    private FoodManageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_mon_an);

        rvFoodList = findViewById(R.id.rvFoodList);
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
    }
}
