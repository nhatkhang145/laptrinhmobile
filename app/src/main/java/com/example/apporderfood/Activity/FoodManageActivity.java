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
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * FoodManageActivity (Màn hình Quản lý Món ăn)
 * Nhiệm vụ chính:
 * - Hiển thị danh sách tất cả các món ăn của nhà hàng.
 * - Cho phép tìm kiếm món ăn theo tên và lọc theo danh mục.
 * - Cung cấp giao diện (BottomSheet) để thêm mới hoặc chỉnh sửa món ăn (bao gồm tải ảnh lên).
 * - Thống kê số lượng món ăn (tổng số, còn món, hết món).
 */
public class FoodManageActivity extends AppCompatActivity {

    private RecyclerView rvFoodList, rvCategoryList;
    private FoodManageAdapter adapter;
    private com.example.apporderfood.adapter.CategoryFilterAdapter categoryAdapter;
    private FloatingActionButton fabAddFood;
    private ProgressBar progressBar;
    private android.widget.TextView tvTotalItems, tvAvailableItems, tvOutOfStockItems;

    private int resId = -1;

    private List<com.example.apporderfood.model.Category> currentCategories = new ArrayList<>();
    private List<com.example.apporderfood.model.Unit> currentUnits = new ArrayList<>();
    private boolean isAvailable = true;
    private String currentKeyword = "";
    private Integer currentFilterCatId = null;

    private android.net.Uri selectedImageUri = null;
    private android.widget.ImageView ivFoodPreview;
    private View layoutUploadPlaceholder;

    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> imagePickerLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            selectedImageUri = result.getData().getData();
                            if (ivFoodPreview != null && layoutUploadPlaceholder != null) {
                                com.bumptech.glide.Glide.with(this).load(selectedImageUri).into(ivFoodPreview);
                                ivFoodPreview.setVisibility(View.VISIBLE);
                                layoutUploadPlaceholder.setVisibility(View.GONE);
                            }
                        }
                    });

    private java.io.File getFileFromUri(android.net.Uri uri) {
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            java.io.File tempFile = java.io.File.createTempFile("upload", ".jpg", getCacheDir());
            java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) out.write(buf, 0, len);
            out.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quan_ly_mon_an);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Lấy resId từ SharedPreferences (đã lưu sau khi đăng nhập)
        SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        resId = prefs.getInt("RES_ID", -1);

        rvFoodList  = findViewById(R.id.rvFoodList);
        rvCategoryList = findViewById(R.id.rvCategoryList);
        fabAddFood  = findViewById(R.id.fab_add_food);
        tvTotalItems = findViewById(R.id.tvTotalItems);
        tvAvailableItems = findViewById(R.id.tvAvailableItems);
        tvOutOfStockItems = findViewById(R.id.tvOutOfStockItems);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        rvFoodList.setLayoutManager(new LinearLayoutManager(this));
        rvCategoryList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Lấy danh sách danh mục để hiển thị lên thanh lọc ngang (Category filter)
        loadCategoriesForFilter();

        // Lấy danh sách món ăn (lần đầu sẽ không có từ khóa tìm kiếm)
        loadMenuFromApi(currentKeyword);

        // Xử lý sự kiện khi người dùng nhập từ khóa tìm kiếm món ăn
        android.widget.EditText etSearchFood = findViewById(R.id.etSearchFood);
        etSearchFood.addTextChangedListener(new android.text.TextWatcher() {
            private android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
            private Runnable runnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (runnable != null) handler.removeCallbacks(runnable);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                currentKeyword = s.toString().trim();
                runnable = () -> loadMenuFromApi(currentKeyword);
                handler.postDelayed(runnable, 300); // 300ms debounce để tránh gọi API liên tục
            }
        });

        fabAddFood.setOnClickListener(v -> showFoodBottomSheet(null));

        setupBottomNav();
    }

    /**
     * Gọi API lấy danh sách danh mục để tạo bộ lọc ngang phía trên cùng
     */
    private void loadCategoriesForFilter() {
        if (resId == -1) return;
        ZappyApiService api = RetrofitClient.getApiService();
        api.getCategories(resId).enqueue(new Callback<List<com.example.apporderfood.model.Category>>() {
            @Override
            public void onResponse(Call<List<com.example.apporderfood.model.Category>> call, Response<List<com.example.apporderfood.model.Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<com.example.apporderfood.model.Category> filterList = new ArrayList<>();
                    com.example.apporderfood.model.Category allCat = new com.example.apporderfood.model.Category();
                    allCat.setId(null);
                    allCat.setCatName("Tất cả");
                    filterList.add(allCat);
                    filterList.addAll(response.body());

                    categoryAdapter = new com.example.apporderfood.adapter.CategoryFilterAdapter(filterList, category -> {
                        currentFilterCatId = category.getId();
                        loadMenuFromApi(currentKeyword);
                    });
                    rvCategoryList.setAdapter(categoryAdapter);
                }
            }
            @Override
            public void onFailure(Call<List<com.example.apporderfood.model.Category>> call, Throwable t) {}
        });
    }

    /**
     * Gọi API GET /api/menu-items/restaurant/{resId}
     * Lấy danh sách món ăn từ database, có hỗ trợ tìm kiếm theo từ khóa
     * và lọc theo danh mục đã chọn (currentFilterCatId).
     */
    private void loadMenuFromApi(String keyword) {
        if (resId == -1) {
            Toast.makeText(this, "Không xác định được nhà hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        ZappyApiService api = RetrofitClient.getApiService();
        api.getMenuByRestaurant(resId, keyword).enqueue(new Callback<List<MenuItem>>() {
            @Override
            public void onResponse(Call<List<MenuItem>> call, Response<List<MenuItem>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<MenuItem> menuItems = response.body();
                    List<FoodManageAdapter.FoodItem> foodList = new ArrayList<>();

                    NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));

                    int total = 0;
                    int available = 0;

                    for (MenuItem item : menuItems) {
                        if (currentFilterCatId != null && (item.getCategory() == null || !item.getCategory().getId().equals(currentFilterCatId))) {
                            continue;
                        }
                        String tenMon   = item.getItemName();
                        String danhMuc  = (item.getCategory() != null) ? item.getCategory().getCatName() : "—";
                        String donVi    = (item.getUnit() != null)     ? item.getUnit().getUnitName()    : "";
                        String giaHien  = fmt.format(item.getPrice()) + "đ";

                        boolean isAvail = (item.getIsAvailable() != null) ? item.getIsAvailable() : true;
                        if (isAvail) available++;
                        String statusStr = isAvail ? "CÒN MÓN" : "HẾT MÓN";

                        foodList.add(new FoodManageAdapter.FoodItem(
                                tenMon, danhMuc, donVi, giaHien, statusStr, item
                        ));
                        total++;
                    }
                    
                    int outOfStock = total - available;
                    if (tvTotalItems != null) tvTotalItems.setText(String.valueOf(total));
                    if (tvAvailableItems != null) tvAvailableItems.setText(String.valueOf(available));
                    if (tvOutOfStockItems != null) tvOutOfStockItems.setText(String.valueOf(outOfStock));

                    adapter = new FoodManageAdapter(foodList, foodItem -> showFoodBottomSheet(foodItem.getRawItem()));
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

    /**
     * Gọi API lấy danh sách Danh mục và Đơn vị tính (Unit) để đổ vào Spinner
     * bên trong form thêm/sửa món ăn.
     */
    private void loadCategoriesAndUnits(Spinner spinnerCategory, MenuItem existingFood) {
        ZappyApiService api = RetrofitClient.getApiService();
        api.getCategories(resId).enqueue(new Callback<List<com.example.apporderfood.model.Category>>() {
            @Override
            public void onResponse(Call<List<com.example.apporderfood.model.Category>> call, Response<List<com.example.apporderfood.model.Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentCategories = response.body();
                    List<String> catNames = new ArrayList<>();
                    int selectedIndex = 0;
                    for (int i = 0; i < currentCategories.size(); i++) {
                        com.example.apporderfood.model.Category c = currentCategories.get(i);
                        catNames.add(c.getCatName());
                        if (existingFood != null && existingFood.getCategory() != null && existingFood.getCategory().getId().equals(c.getId())) {
                            selectedIndex = i;
                        }
                    }
                    ArrayAdapter<String> adapterCategory = new ArrayAdapter<>(FoodManageActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, catNames);
                    spinnerCategory.setAdapter(adapterCategory);
                    spinnerCategory.setSelection(selectedIndex);
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

    /**
     * Hiển thị BottomSheet (khung trượt từ dưới lên) chứa form Thêm mới hoặc Chỉnh sửa món ăn.
     * @param existingFood null nếu là thêm mới, ngược lại là dữ liệu món ăn cần sửa.
     */
    private void showFoodBottomSheet(MenuItem existingFood) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.activity_them_mon_an, null);
        bottomSheetDialog.setContentView(view);

        // Fix the jumping issue
        com.google.android.material.bottomsheet.BottomSheetBehavior<View> behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from((View) view.getParent());
        behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);
        behavior.setHideable(false); // Initially false to prevent light flings
        
        behavior.addBottomSheetCallback(new com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                if (newState == com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED) {
                    behavior.setHideable(false);
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
                // Khi kéo xuống dưới một nửa (slideOffset < 0.5), mới cho phép ẩn
                if (slideOffset < 0.5f) {
                    behavior.setHideable(true);
                } else {
                    behavior.setHideable(false);
                }
            }
        });

        View layoutUploadImage = view.findViewById(R.id.layout_upload_image);
        ivFoodPreview = view.findViewById(R.id.iv_food_preview);
        layoutUploadPlaceholder = view.findViewById(R.id.layout_upload_placeholder);

        // Xử lý sự kiện nhấn vào khu vực tải ảnh để chọn ảnh từ thư viện
        layoutUploadImage.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Điền dữ liệu nếu là chế độ chỉnh sửa (existingFood != null)
        selectedImageUri = null;
        if (existingFood != null && existingFood.getImageUrl() != null) {
            com.bumptech.glide.Glide.with(this).load(existingFood.getImageUrl()).into(ivFoodPreview);
            ivFoodPreview.setVisibility(View.VISIBLE);
            layoutUploadPlaceholder.setVisibility(View.GONE);
        } else {
            ivFoodPreview.setVisibility(View.GONE);
            layoutUploadPlaceholder.setVisibility(View.VISIBLE);
        }

        Spinner spinnerCategory = view.findViewById(R.id.spinner_category);
        loadCategoriesAndUnits(spinnerCategory, existingFood);

        android.widget.EditText edtFoodName = view.findViewById(R.id.edt_food_name);
        android.widget.EditText edtPrice = view.findViewById(R.id.edt_price);

        view.findViewById(R.id.btn_close).setOnClickListener(v -> bottomSheetDialog.dismiss());
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> bottomSheetDialog.dismiss());
        
        setupStatusToggle(view);

        if (existingFood != null) {
            ((android.widget.TextView) view.findViewById(R.id.tv_title)).setText("Chỉnh sửa món ăn");
            ((com.google.android.material.button.MaterialButton) view.findViewById(R.id.btn_save)).setText("Cập nhật");
            edtFoodName.setText(existingFood.getItemName());
            if (existingFood.getPrice() != null) {
                edtPrice.setText(String.valueOf(existingFood.getPrice().intValue()));
            }
        }

        // Xử lý sự kiện khi nhấn nút LƯU
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

            // Nếu có ảnh mới được chọn, gọi API upload ảnh trước
            ZappyApiService api = RetrofitClient.getApiService();
            if (selectedImageUri != null) {
                java.io.File file = getFileFromUri(selectedImageUri);
                if (file != null) {
                    okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(okhttp3.MediaType.parse("image/*"), file);
                    okhttp3.MultipartBody.Part body = okhttp3.MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                    api.uploadImage(body).enqueue(new Callback<Map<String, String>>() {
                        @Override
                        public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                data.put("imageUrl", response.body().get("url"));
                                saveFoodData(api, data, existingFood, bottomSheetDialog);
                            } else {
                                Toast.makeText(FoodManageActivity.this, "Lỗi upload ảnh", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<Map<String, String>> call, Throwable t) {
                            Toast.makeText(FoodManageActivity.this, "Lỗi kết nối upload", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                saveFoodData(api, data, existingFood, bottomSheetDialog);
            }
        });

        bottomSheetDialog.show();
    }

    /**
     * Gọi API để lưu dữ liệu món ăn (thêm mới hoặc cập nhật) lên server sau khi
     * đã chuẩn bị xong payload (dữ liệu + url ảnh nếu có).
     */
    private void saveFoodData(ZappyApiService api, java.util.Map<String, Object> data, MenuItem existingFood, BottomSheetDialog bottomSheetDialog) {
        if (existingFood == null) {
            // Chế độ Thêm mới
            api.createMenuItem(data).enqueue(new Callback<MenuItem>() {
                @Override
                public void onResponse(Call<MenuItem> call, Response<MenuItem> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(FoodManageActivity.this, "Thêm món thành công!", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                        loadMenuFromApi(currentKeyword);
                    } else {
                        Toast.makeText(FoodManageActivity.this, "Lỗi thêm món: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<MenuItem> call, Throwable t) {
                    Toast.makeText(FoodManageActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            api.updateMenuItem(existingFood.getId(), data).enqueue(new Callback<MenuItem>() {
                @Override
                public void onResponse(Call<MenuItem> call, Response<MenuItem> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(FoodManageActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                        loadMenuFromApi(currentKeyword);
                    } else {
                        Toast.makeText(FoodManageActivity.this, "Lỗi cập nhật: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<MenuItem> call, Throwable t) {
                    Toast.makeText(FoodManageActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupStatusToggle(View view) {
        View available = view.findViewById(R.id.tv_status_available);
        View soldOut   = view.findViewById(R.id.tv_status_sold_out);

        isAvailable = true;

        View.OnClickListener listener = v -> {
            available.setBackground(null);
            soldOut.setBackground(null);
            ((android.widget.TextView) available).setTextColor(getColor(R.color.text_secondary));
            ((android.widget.TextView) soldOut).setTextColor(getColor(R.color.text_secondary));

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
