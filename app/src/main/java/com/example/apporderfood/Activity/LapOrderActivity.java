package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apporderfood.R;
import com.example.apporderfood.adapter.MenuItemLapOrderAdapter;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.Category;
import com.example.apporderfood.model.CartItem;
import com.example.apporderfood.model.MenuItem;
import com.google.android.material.tabs.TabLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LapOrderActivity extends AppCompatActivity {

    private LinearLayout btnHuyBo;
    private LinearLayout btnDongY;
    private TabLayout tabLayoutCategories;
    private RecyclerView rvMenuItems;
    private EditText etSearch;

    private int tableId = -1;
    private String tableName = "";
    private int orderId = -1; // Chỉ được set SAU KHI ấn ĐỒNG Ý và openTable() thành công
    private int resId = -1;

    public static LapOrderActivity instance;

    private MenuItemLapOrderAdapter adapter;
    private List<Category> categoryList = new ArrayList<>();
    private ZappyApiService apiService;
    private Integer currentSelectedCatId = null; // null = Tất cả
    private int currentUserId = -1;
    private int currentUserRole = 0;

    @Override

    //B1
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lap_order);

        // Nhận dữ liệu từ SoDobanActivity
        tableId = getIntent().getIntExtra("TABLE_ID", -1); // lấy ID bàn dc truyền sang
        tableName = getIntent().getStringExtra("TABLE_NAME") != null
                ? getIntent().getStringExtra("TABLE_NAME")
                : "Bàn";

        android.content.SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        resId = prefs.getInt("RES_ID", -1);
        currentUserId = prefs.getInt("USER_ID", -1);
        currentUserRole = prefs.getInt("ROLE", 0);
        apiService = RetrofitClient.getApiService();

        initViews();
        setupRecyclerView();
        setupClickListeners();
        setupSearch();

        instance = this;

        // B2 Xóa giỏ hàng cũ khi mở bàn mới (tránh lưu giỏ hàng của bàn trước)
        cartMap.clear();

        //B3 ktra quyền
        checkShiftPermission();
    }

    private void checkShiftPermission() {
        // B4 Xem nv có đang trong ca không
        // Gọi API lấy ca làm việc đang hoạt động của nhà hàng
        apiService.getActiveShift(resId).enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call, retrofit2.Response<java.util.Map<String, Object>> response) {
                // Có ca làm việc đang mở
                if (response.isSuccessful() && response.body() != null) {
                    java.util.Map<String, Object> shift = response.body();
                    // Lấy chuỗi employeeIds
                    String employeeIds = "";
                    if (shift.get("employeeIds") != null) {
                        employeeIds = (String) shift.get("employeeIds");
                    }
                    // Kiểm tra nhân viên hiện tại có nằm trong ca hay không
                    boolean isInShift = false;
                    String[] ids = employeeIds.split(",");
                    for (String idStr : ids) {
                        try {
                            if (Integer.parseInt(idStr.trim()) == currentUserId) {
                                isInShift = true;
                                break;
                            }
                        } catch (NumberFormatException ignored) {}// Bỏ qua nếu employeeIds chứa dữ liệu không hợp lệ
                    }
                    // Không thuộc ca và không phải Admin -> từ chối gọi món
                    if (!isInShift && currentUserRole != 1) {
                        showErrorAndExit("Bạn không nằm trong ca làm việc hiện tại, không thể gọi món!");
                    } else {
                        //B5 Có quyền gọi món -> tải danh mục món ăn
                        loadCategories();
                    }
                } else {
                    // Không có ca làm việc nào đang mở
                    if (currentUserRole != 1) {
                        // Nhân viên thường không được gọi món khi chưa mở ca
                        showErrorAndExit("Chưa mở ca làm việc! Vui lòng liên hệ Quản lý để mở ca.");
                    } else {
                        // Admin vẫn được phép truy cập
                        loadCategories();
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) {
                // Lỗi kết nối hoặc gọi API thất bại
                showErrorAndExit("Lỗi kiểm tra ca làm việc: " + t.getMessage());
            }
        });
    }

    /**
     * HIỂN THỊ THÔNG BÁO LỖI VÀ ĐÓNG MÀN HÌNH
     * Dùng khi nhân viên không có quyền gọi món hoặc có lỗi nghiêm trọng.
     */
    private void showErrorAndExit(String message) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Cảnh báo")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Đóng", (dialog, which) -> finish())
                .show();
    }

    /**
     * KHỞI TẠO CÁC THÀNH PHẦN GIAO DIỆN (Ánh xạ ID)
     * Đặt tên cho màn hình dựa theo tên bàn được truyền sang.
     */
    private void initViews() {
        btnHuyBo = findViewById(R.id.btnHuyBo);
        btnDongY = findViewById(R.id.btnDongY);
        tabLayoutCategories = findViewById(R.id.tabLayoutCategories);
        rvMenuItems = findViewById(R.id.rvMenuItems);
        etSearch = findViewById(R.id.etSearch);

        // Cập nhật title Header
        LinearLayout headerLayout = findViewById(R.id.headerLayout);
        if (headerLayout != null && headerLayout.getChildCount() > 1) {
            View child = headerLayout.getChildAt(1);
            if (child instanceof TextView) {
                ((TextView) child).setText("Lập Order - " + tableName);
            }
        }
    }

    // BIẾN QUAN TRỌNG: Giỏ hàng tạm thời (Session Cart) lưu trên RAM.
    // Dùng static để có thể truy cập từ Adapter và XacNhanOrderActivity mà không cần gửi dữ liệu phức tạp.
    // Key: ID món ăn | Value: Thông tin món (CartItem bao gồm số lượng, ghi chú)
    public static Map<Integer, CartItem> cartMap = new HashMap<>();

    /**
     * THIẾT LẬP RECYCLER VIEW DANH SÁCH MÓN ĂN
     * Truyền biến cartMap vào Adapter để Adapter biết món nào đang được chọn.
     */
    private void setupRecyclerView() {
        adapter = new MenuItemLapOrderAdapter(this, new ArrayList<>(), cartMap, updatedCart -> {
            cartMap = updatedCart;
        });
        rvMenuItems.setLayoutManager(new LinearLayoutManager(this));
        rvMenuItems.setAdapter(adapter);
    }

    /**
     * TẢI DANH MỤC MÓN ĂN (Categories)
     * Gọi API lấy danh sách loại món, nếu thành công thì tạo các Tab tương ứng.
     */
    private void loadCategories() {
        if (resId == -1)
            return;

        apiService.getCategories(resId).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList = response.body();
                    setupTabs();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(LapOrderActivity.this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** B6
     * THIẾT LẬP CÁC TAB DANH MỤC (Tất cả, Đồ ăn, Nước uống,...)
     */
    private void setupTabs() {
        tabLayoutCategories.removeAllTabs();

        // Tab "Tất cả"
        tabLayoutCategories.addTab(tabLayoutCategories.newTab().setText("Tất cả"));

        for (Category cat : categoryList) {
            tabLayoutCategories.addTab(tabLayoutCategories.newTab().setText(cat.getCatName()));
        }

        tabLayoutCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    currentSelectedCatId = null;
                } else if (position - 1 < categoryList.size()) {
                    currentSelectedCatId = categoryList.get(position - 1).getId();
                }
                //B7 gọi lại API tải danh sách món ăn lên
                loadMenuItems(etSearch.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // Tải danh sách món cho "Tất cả" mặc định ban đầu
        loadMenuItems("");
    }


    private void loadMenuItems(String keyword) {
        if (resId == -1)
            return;

        Call<List<MenuItem>> call;
        if (currentSelectedCatId == null) {
            //B8 Gọi API tải danh sách món ăn
            call = apiService.getMenuByRestaurant(resId, keyword);
        } else {
            if (keyword != null && !keyword.trim().isEmpty()) {
                call = apiService.getMenuByRestaurant(resId, keyword);
            } else {
                call = apiService.getMenuByCategory(currentSelectedCatId);
            }
        }

        //B9 Khi api trả về danh sách -> bắn dữ liệu sang MenuItemsAdapter để vẽ lên màn hình
        call.enqueue(new Callback<List<MenuItem>>() {
            @Override
            public void onResponse(Call<List<MenuItem>> call, Response<List<MenuItem>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    //Gọi hàm cập nhật danh sách món ăn
                    adapter.setMenuItems(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<MenuItem>> call, Throwable t) {
                Toast.makeText(LapOrderActivity.this, "Lỗi tải món ăn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * THIẾT LẬP Ô TÌM KIẾM MÓN ĂN (Debounce Search)
     * Chờ 400ms sau khi ngừng gõ mới gọi API để tránh làm quá tải Server.
     */
    private void setupSearch() {
        android.os.Handler searchHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        Runnable[] searchRunnable = {null};

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable[0] != null) {
                    searchHandler.removeCallbacks(searchRunnable[0]);
                }
                String keyword = s.toString();
                searchRunnable[0] = () -> loadMenuItems(keyword);
                searchHandler.postDelayed(searchRunnable[0], 400);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * LẮNG NGHE CÁC NÚT BẤM (Hủy bỏ, Đồng ý, Back)
     */
    //B16 Bấm Đồng ý -> có món thì gọi hàm createOrderThenAddItems()
    private void setupClickListeners() {
        btnHuyBo.setOnClickListener(v -> finish());

        btnDongY.setOnClickListener(v -> {
            //B17 ktra chưa có món thì bị chặn lại
            if (cartMap.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 món!", Toast.LENGTH_SHORT).show();
                return;
            }
            btnDongY.setEnabled(false);

            // Có món thì gọi hàm này
            createOrderThenAddItems();
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    /**
     * HÀM TẠO ORDER MỚI
     * - Được gọi khi bấm nút "ĐỒNG Ý".
     * - Nhiệm vụ: Gọi API mở bàn (tạo Order mới), chuyển trạng thái bàn sang "Có Khách".
     * - Chưa gửi món xuống bếp ở bước này, món vẫn chỉ nằm trong biến cartMap.
     */

    //Gọi hàm
    private void createOrderThenAddItems() {
        if (tableId == -1) {
            btnDongY.setEnabled(true);
            return;
        }

        ZappyApiService api = RetrofitClient.getApiService();
        Map<String, Integer> data = new HashMap<>();
        data.put("tableId", tableId);

        android.content.SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        int userId = prefs.getInt("USER_ID", -1);
        if (userId != -1) {
            data.put("userId", userId);
        }

        Toast.makeText(this, "Đang tạo đơn...", Toast.LENGTH_SHORT).show();

        //B18 Gọi API mở bàn chính thức báo cho DB biết bàn này đổi trạng thái đã có khách
        api.openTable(data).enqueue(new Callback<Map>() {
            @Override
            public void onResponse(Call<Map> call, Response<Map> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Object id = response.body().get("id");
                    if (id == null) id = response.body().get("orderId");
                    if (id != null) {
                        // Lấy ID của Order vừa được tạo
                        orderId = ((Double) id).intValue();
                        //B20 Khi API trả vê thaành cộng công -> gọi hàm addItemsToOrder()
                        addItemsToOrder();
                    } else {
                        Toast.makeText(LapOrderActivity.this, "Lỗi: Không nhận được mã đơn!", Toast.LENGTH_SHORT).show();
                        btnDongY.setEnabled(true);
                    }
                } else if (response.code() == 400) {
                    fetchActiveOrderThenAddItems();
                } else {
                    String errMsg = "Lỗi " + response.code();
                    try {
                        if (response.errorBody() != null) errMsg += ": " + response.errorBody().string();
                    } catch (Exception ignored) {}
                    Toast.makeText(LapOrderActivity.this, errMsg, Toast.LENGTH_LONG).show();
                    btnDongY.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<Map> call, Throwable t) {
                Toast.makeText(LapOrderActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                btnDongY.setEnabled(true);
            }
        });
    }

    /**
     * HÀM DỰ PHÒNG: LẤY ORDER HIỆN TẠI CỦA BÀN
     * Dùng trong trường hợp API openTable báo lỗi 400 (Bàn đã được mở trước đó).
     */
    private void fetchActiveOrderThenAddItems() {
        RetrofitClient.getApiService().getActiveOrder(tableId).enqueue(new Callback<Map>() {
            @Override
            public void onResponse(Call<Map> call, Response<Map> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Object id = response.body().get("id");
                    if (id == null) id = response.body().get("orderId");
                    if (id != null) {
                        orderId = ((Double) id).intValue();
                        addItemsToOrder();
                    } else {
                        Toast.makeText(LapOrderActivity.this, "Không lấy được đơn hiện tại!", Toast.LENGTH_SHORT).show();
                        btnDongY.setEnabled(true);
                    }
                } else {
                    Toast.makeText(LapOrderActivity.this, "Bàn đang có khách nhưng không tìm được đơn!", Toast.LENGTH_LONG).show();
                    btnDongY.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<Map> call, Throwable t) {
                Toast.makeText(LapOrderActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                btnDongY.setEnabled(true);
            }
        });
    }

    /**
     * HÀM CHUYỂN SANG MÀN HÌNH XÁC NHẬN ORDER
     * Chuyển Order ID và thông tin Bàn sang màn hình XacNhanOrderActivity.
     */

    //B21 Dùng Intent đống gói orderID, tableID, tableName và chuyển luồng sang XacNhanOrderActivity
    private void addItemsToOrder() {
        btnDongY.setEnabled(true);
        Intent intent = new Intent(LapOrderActivity.this, XacNhanOrderActivity.class);
        intent.putExtra("ORDER_ID", orderId);
        intent.putExtra("TABLE_NAME", tableName);
        intent.putExtra("TABLE_ID", tableId);
        startActivity(intent);
    }

    /**
     * HÀM DỌN DẸP KHI ĐÓNG MÀN HÌNH
     * Xóa tham chiếu instance để tránh memory leak.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (instance == this) {
            instance = null;
        }
    }
}
