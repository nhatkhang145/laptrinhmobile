package com.example.apporderfood.Activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apporderfood.R;
import com.example.apporderfood.adapter.CategoryManageAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class CategoryManageActivity extends AppCompatActivity {

    private RecyclerView rvCategoryList;
    private CategoryManageAdapter adapter;
    private FloatingActionButton fabAddCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_danh_muc);

        rvCategoryList = findViewById(R.id.rvCategoryList);
        fabAddCategory = findViewById(R.id.fab_add_category);
        
        rvCategoryList.setLayoutManager(new LinearLayoutManager(this));

        List<CategoryManageAdapter.CategoryItem> categoryList = new ArrayList<>();
        categoryList.add(new CategoryManageAdapter.CategoryItem("Burger", "HOẠT ĐỘNG", 45, "Các loại burger bò, gà..."));
        categoryList.add(new CategoryManageAdapter.CategoryItem("Pizza", "HOẠT ĐỘNG", 32, "Pizza truyền thống Ý"));
        categoryList.add(new CategoryManageAdapter.CategoryItem("Đồ uống", "HOẠT ĐỘNG", 58, "Nước ngọt, cà phê, trà"));
        categoryList.add(new CategoryManageAdapter.CategoryItem("Tráng miệng", "TẠM ẨN", 12, "Kem, bánh ngọt, chè"));
        categoryList.add(new CategoryManageAdapter.CategoryItem("Combo", "HOẠT ĐỘNG", 8, "Tiết kiệm cho nhóm"));
        categoryList.add(new CategoryManageAdapter.CategoryItem("Món ăn nhanh", "TẠM ẨN", 24, "Khoai tây chiên, snack"));
        categoryList.add(new CategoryManageAdapter.CategoryItem("Mì cay", "HOẠT ĐỘNG", 15, "Mì cay 7 cấp độ"));
        categoryList.add(new CategoryManageAdapter.CategoryItem("Lẩu", "HOẠT ĐỘNG", 10, "Lẩu Thái, lẩu hải sản"));

        adapter = new CategoryManageAdapter(categoryList);
        rvCategoryList.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        fabAddCategory.setOnClickListener(v -> showAddCategoryBottomSheet());

        setupBottomNav();
    }

    private void showAddCategoryBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.activity_them_danh_muc, null);
        bottomSheetDialog.setContentView(view);

        EditText edtName = view.findViewById(R.id.edt_category_name);
        TextView tvPreviewName = view.findViewById(R.id.tv_preview_name);

        // Real-time preview feature
        edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    tvPreviewName.setText(s.toString());
                } else {
                    tvPreviewName.setText("Tên danh mục");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        view.findViewById(R.id.btn_close).setOnClickListener(v -> bottomSheetDialog.dismiss());
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> bottomSheetDialog.dismiss());
        view.findViewById(R.id.btn_save).setOnClickListener(v -> {
            // Save logic here
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void setupBottomNav() {
        android.view.View navOrder = findViewById(R.id.navOrder);
        android.view.View navSoDo = findViewById(R.id.navSoDo);
        android.view.View navTienIch = findViewById(R.id.navTienIch);

        if (navOrder != null) {
            navOrder.setOnClickListener(v -> {
                startActivity(new android.content.Intent(this, DanhSachOrderActivity.class));
                overridePendingTransition(0, 0);
            });
        }
        if (navSoDo != null) {
            navSoDo.setOnClickListener(v -> {
                startActivity(new android.content.Intent(this, SoDobanActivity.class));
                overridePendingTransition(0, 0);
            });
        }
        if (navTienIch != null) {
            navTienIch.setOnClickListener(v -> {
                startActivity(new android.content.Intent(this, TienIchActivity.class));
                overridePendingTransition(0, 0);
                finish();
            });
        }
    }
}
