package com.example.apporderfood.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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
import com.example.apporderfood.adapter.AreaManageAdapter;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.Area;
import com.example.apporderfood.model.TableModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ============================================================
 *  AREA MANAGE ACTIVITY - QUẢN LÝ KHU VỰC (DÀNH CHO QUẢN LÝ)
 * ============================================================
 *
 * Màn hình cho Quản lý xem và quản lý tất cả khu vực của nhà hàng.
 * Mỗi khu vực (Area) chứa nhiều bàn (Table).
 *
 * Chức năng:
 *   - Xem danh sách khu vực (có tên và số bàn của từng khu)
 *   - Thống kê tổng số / đang hoạt động / đang ẩn
 *   - Tìm kiếm khu vực theo tên
 *   - Thêm khu vực mới (FAB +) → ThemKhuVucActivity
 *   - Sửa khu vực → ThemKhuVucActivity (chế độ edit)
 *   - Ẩn/Hiện khu vực (Toggle) → API PATCH
 *   - Xóa khu vực → Xác nhận AlertDialog → API DELETE
 *   - Xem chi tiết khu vực → ChiTietKhuVucActivity
 *
 * Implements AreaManageAdapter.OnAreaItemClickListener:
 *   Nhận sự kiện click từ từng item trong RecyclerView
 *   (Edit / Toggle status / Delete / Click để xem chi tiết)
 */
public class AreaManageActivity extends AppCompatActivity
        implements AreaManageAdapter.OnAreaItemClickListener {

    // ---- RecyclerView và Adapter ----
    private RecyclerView rvAreaList;       // Danh sách khu vực
    private AreaManageAdapter adapter;     // Adapter hiển thị từng khu vực

    // ---- Các nút điều khiển ----
    private FloatingActionButton fabAddArea; // Nút + nổi để thêm khu vực mới
    private ProgressBar pbLoading;           // Vòng loading khi đang gọi API

    // ---- Hiển thị trạng thái ----
    private TextView tvEmptyState;   // "Chưa có khu vực nào" khi danh sách rỗng

    // ---- Thống kê tổng quan ----
    private TextView tvTotalAreas;   // Tổng số khu vực
    private TextView tvActiveAreas;  // Số khu vực đang hoạt động
    private TextView tvHiddenAreas;  // Số khu vực đang ẩn

    // ---- Tìm kiếm ----
    private EditText etSearch; // Ô tìm kiếm theo tên khu vực

    // ID nhà hàng (đọc từ SharedPreferences sau khi login)
    private int resId = -1;

    /**
     * Launcher để mở ThemKhuVucActivity và nhận kết quả trả về.
     *
     * Dùng ActivityResultLauncher thay vì startActivityForResult() (đã deprecated).
     * Khi ThemKhuVucActivity trả về RESULT_OK → tải lại danh sách khu vực (loadAreas())
     */
    private final ActivityResultLauncher<Intent> addAreaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadAreas(); // Thêm/sửa thành công → refresh danh sách
                }
            }
    );

    // ============================================================
    //  VÒNG ĐỜI ACTIVITY
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quan_ly_khu_vuc);

        // Xử lý padding tránh bị che bởi thanh hệ thống
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Lấy ID nhà hàng từ SharedPreferences (được lưu khi đăng nhập)
        SharedPreferences prefs = getSharedPreferences("ZappySession", MODE_PRIVATE);
        resId = prefs.getInt("RES_ID", -1);

        initViews();           // Bước 1: Ánh xạ view
        setupRecyclerView();   // Bước 2: Thiết lập RecyclerView
        loadAreas();           // Bước 3: Gọi API lấy danh sách khu vực
        setupListeners();      // Bước 4: Gán sự kiện click
        setupBottomNav();      // Bước 5: Gán sự kiện điều hướng dưới cùng
    }

    // ============================================================
    //  BƯỚC 1: ÁNH XẠ VIEW
    // ============================================================

    /**
     * Ánh xạ tất cả View từ layout XML vào biến Java.
     */
    private void initViews() {
        rvAreaList    = findViewById(R.id.rvAreaList);
        fabAddArea    = findViewById(R.id.fab_add_Area);
        pbLoading     = findViewById(R.id.pbAreaLoading);
        tvEmptyState  = findViewById(R.id.tvAreaEmptyState);
        tvTotalAreas  = findViewById(R.id.tvTotalAreas);
        tvActiveAreas = findViewById(R.id.tvActiveAreas);
        tvHiddenAreas = findViewById(R.id.tvHiddenAreas);
        etSearch      = findViewById(R.id.etSearch);
    }

    // ============================================================
    //  BƯỚC 2: THIẾT LẬP RECYCLERVIEW
    // ============================================================

    /**
     * Thiết lập layout manager cho RecyclerView (danh sách dọc).
     * Adapter sẽ được gán sau khi có dữ liệu từ API.
     */
    private void setupRecyclerView() {
        if (rvAreaList != null) {
            rvAreaList.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    // ============================================================
    //  BƯỚC 3: GỌI API LẤY DỮ LIỆU
    // ============================================================

    /**
     * Gọi API lấy danh sách khu vực của nhà hàng, sau đó gọi thêm API
     * lấy tất cả bàn để tính số lượng bàn trong mỗi khu vực.
     *
     * Luồng gọi API (2 cấp lồng nhau):
     *
     *   Bước A: GET /api/areas/restaurant/{resId}
     *             → Lấy danh sách khu vực (Area[])
     *             → Nếu danh sách rỗng → hiện empty state, dừng
     *             → Nếu có dữ liệu → tiếp tục Bước B
     *
     *   Bước B: GET /api/tables/restaurant/{resId}
     *             → Lấy tất cả bàn của nhà hàng
     *             → Duyệt từng bàn, đếm và ghép tên vào khu vực tương ứng
     *             → Gọi displayAreas() để hiển thị kết quả lên RecyclerView
     *
     * Lý do cần 2 API:
     *   API khu vực không trả về thông tin bàn → cần join thủ công ở client
     */
    private void loadAreas() {
        if (resId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin nhà hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị loading, ẩn danh sách
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (rvAreaList != null) rvAreaList.setVisibility(View.GONE);
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);

        ZappyApiService api = RetrofitClient.getApiService();

        // ---- Bước A: Lấy danh sách khu vực ----
        // API: GET /api/areas/restaurant/{resId}
        api.getAreas(resId).enqueue(new Callback<List<Area>>() {
            @Override
            public void onResponse(Call<List<Area>> call, Response<List<Area>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Area> AreaList = response.body();

                    if (AreaList.isEmpty()) {
                        // Chưa có khu vực nào → hiện empty state
                        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                        if (tvEmptyState != null) tvEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        // ---- Bước B: Lấy danh sách bàn để đếm số bàn/khu vực ----
                        // API: GET /api/tables/restaurant/{resId}
                        api.getAllTablesByRestaurant(resId).enqueue(new Callback<List<TableModel>>() {
                            @Override
                            public void onResponse(Call<List<TableModel>> call,
                                                   Response<List<TableModel>> responseMenu) {
                                if (pbLoading != null) pbLoading.setVisibility(View.GONE);

                                if (responseMenu.isSuccessful() && responseMenu.body() != null) {
                                    List<TableModel> tables = responseMenu.body();

                                    // Ghép thông tin số bàn vào từng khu vực
                                    // Duyệt từng khu vực → đếm bàn thuộc khu vực đó
                                    for (Area cat : AreaList) {
                                        int count = 0;
                                        StringBuilder names = new StringBuilder();

                                        for (TableModel item : tables) {
                                            // Kiểm tra bàn có thuộc khu vực này không
                                            if (item.getArea() != null
                                                    && item.getArea().getId() != null
                                                    && item.getArea().getId().equals(cat.getId())) {
                                                count++;
                                                // Ghép tên bàn: "Bàn 01, Bàn 02, Bàn 03"
                                                if (names.length() > 0) names.append(", ");
                                                names.append(item.getTableName());
                                            }
                                        }

                                        cat.setItemCount(count);           // Số lượng bàn
                                        cat.setTableNames(names.toString()); // Danh sách tên bàn
                                    }
                                }
                                // Hiển thị kết quả (dù API bàn thất bại vẫn hiện khu vực)
                                displayAreas(AreaList);
                            }

                            @Override
                            public void onFailure(Call<List<TableModel>> call, Throwable t) {
                                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                                // Lỗi lấy bàn → vẫn hiển thị khu vực (không có số bàn)
                                displayAreas(AreaList);
                            }
                        });
                    }
                } else {
                    if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                    Toast.makeText(AreaManageActivity.this, "Lỗi tải khu vực", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Area>> call, Throwable t) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                Toast.makeText(AreaManageActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Hiển thị danh sách khu vực lên RecyclerView và cập nhật thống kê.
     *
     * @param AreaList Danh sách khu vực đã được gắn số bàn
     */
    private void displayAreas(List<Area> AreaList) {
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);

        // Tạo adapter với danh sách và gán listener (this = AreaManageActivity)
        // Activity này sẽ nhận callback khi người dùng click Edit/Delete/Toggle trên từng item
        adapter = new AreaManageAdapter(AreaList, AreaManageActivity.this);

        if (rvAreaList != null) {
            rvAreaList.setVisibility(View.VISIBLE);
            rvAreaList.setAdapter(adapter);
        }

        updateStats(AreaList); // Cập nhật 3 ô thống kê
    }

    /**
     * Cập nhật 3 ô thống kê: Tổng / Đang hoạt động / Đang ẩn.
     *
     * Khu vực "đang hoạt động":  isActive = true hoặc null (mặc định = hoạt động)
     * Khu vực "đang ẩn":         isActive = false
     *
     * @param list Danh sách khu vực hiện tại
     */
    private void updateStats(List<Area> list) {
        int total  = list.size();
        int active = 0;
        int hidden = 0;

        for (Area item : list) {
            if (item.getIsActive() == null || item.getIsActive()) active++;
            else hidden++;
        }

        if (tvTotalAreas != null)  tvTotalAreas.setText(String.valueOf(total));
        if (tvActiveAreas != null) tvActiveAreas.setText(String.valueOf(active));
        if (tvHiddenAreas != null) tvHiddenAreas.setText(String.valueOf(hidden));
    }

    /**
     * Tải lại toàn bộ danh sách từ server (sau khi thêm/sửa/xóa khu vực).
     * Gọi loadAreas() thay vì cập nhật cục bộ để đảm bảo dữ liệu đồng bộ với server.
     */
    private void updateStatsFromAdapter() {
        if (adapter == null) return;
        loadAreas(); // Refresh toàn bộ danh sách từ API
    }

    // ============================================================
    //  BƯỚC 4: SỰ KIỆN CLICK VÀ TÌM KIẾM
    // ============================================================

    /**
     * Gán sự kiện click cho nút Back, FAB thêm khu vực, và ô tìm kiếm.
     */
    private void setupListeners() {
        // Nút Back ← đóng màn hình
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // FAB (+) → mở ThemKhuVucActivity để thêm khu vực mới
        // Dùng addAreaLauncher để nhận kết quả trả về (RESULT_OK → loadAreas)
        if (fabAddArea != null) {
            fabAddArea.setOnClickListener(v -> {
                Intent intent = new Intent(this, ThemKhuVucActivity.class);
                addAreaLauncher.launch(intent);
            });
        }

        // Ô tìm kiếm → lọc real-time khi người dùng gõ
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Gọi adapter.filter() mỗi khi văn bản thay đổi
                    // Adapter tự lọc danh sách theo tên khu vực
                    if (adapter != null) adapter.filter(s.toString());
                }

                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    // ============================================================
    //  CALLBACK TỪ ADAPTER (Interface AreaManageAdapter.OnAreaItemClickListener)
    // ============================================================

    /**
     * Callback: Người dùng bấm nút SỬA trên 1 khu vực.
     *
     * Mở ThemKhuVucActivity ở chế độ chỉnh sửa:
     *   - IS_EDIT = true: Activity biết là đang sửa (không phải thêm mới)
     *   - Area_DATA: Dữ liệu khu vực hiện tại để điền sẵn vào form
     *
     * @param item Khu vực cần sửa
     */
    @Override
    public void onEditClick(Area item) {
        Intent intent = new Intent(this, ThemKhuVucActivity.class);
        intent.putExtra("IS_EDIT", true);      // Báo cho ThemKhuVucActivity biết đang sửa
        intent.putExtra("Area_DATA", item);    // Truyền dữ liệu khu vực hiện tại
        addAreaLauncher.launch(intent); // Dùng launcher để nhận RESULT_OK sau khi sửa
    }

    /**
     * Callback: Người dùng bấm nút TOGGLE (Ẩn/Hiện) trên 1 khu vực.
     *
     * Gọi API đổi trạng thái isActive:
     *   - Đang hoạt động → Tạm ẩn
     *   - Đang ẩn → Hoạt động trở lại
     *
     * Lưu ý: Không thể ẩn khu vực đang có bàn có khách (server trả về 400)
     *
     * @param item     Khu vực cần toggle
     * @param position Vị trí trong RecyclerView (để adapter cập nhật đúng item)
     */
    @Override
    public void onStatusToggleClick(Area item, int position) {
        if (item.getId() == null) return;

        // Xác định trạng thái mới (đảo ngược trạng thái hiện tại)
        boolean currentlyActive = item.getIsActive() == null || item.getIsActive();
        boolean newStatus = !currentlyActive; // Đảo trạng thái
        String newStatusLabel = currentlyActive ? "TẠM ẨN" : "HOẠT ĐỘNG";

        ZappyApiService api = RetrofitClient.getApiService();

        // API: PATCH /api/areas/{id}/toggle (hoặc tương tự)
        api.toggleAreaStatus(item.getId()).enqueue(new Callback<Map>() {
            @Override
            public void onResponse(Call<Map> call, Response<Map> response) {
                if (response.isSuccessful()) {
                    // Thành công → cập nhật UI cục bộ (không gọi lại API)
                    item.setIsActive(newStatus);
                    if (adapter != null) adapter.updateItemStatus(position, newStatus);
                    updateStatsFromAdapter(); // Cập nhật lại thống kê
                    Toast.makeText(AreaManageActivity.this,
                            "\"" + item.getAreaName() + "\" → " + newStatusLabel,
                            Toast.LENGTH_SHORT).show();
                } else if (response.code() == 400) {
                    // Server từ chối: khu vực đang có bàn bận
                    Toast.makeText(AreaManageActivity.this,
                            "Không thể ẩn khu vực đang có bàn có khách!",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AreaManageActivity.this,
                            "Cập nhật thất bại (lỗi " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map> call, Throwable t) {
                Toast.makeText(AreaManageActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Callback: Người dùng bấm nút XÓA trên 1 khu vực.
     *
     * Hiện AlertDialog xác nhận trước khi xóa (tránh xóa nhầm).
     * Nếu xác nhận → gọi performDelete() để gọi API xóa.
     *
     * @param item Khu vực cần xóa
     */
    @Override
    public void onDeleteClick(Area item) {
        // Hiện hộp thoại xác nhận với thông báo cảnh báo
        new AlertDialog.Builder(this)
                .setTitle("Xóa khu vực")
                .setMessage("Bạn có chắc muốn xóa khu vực \"" + item.getAreaName() + "\"?\n"
                        + "Các bàn trong khu vực này sẽ bị ảnh hưởng.")
                .setPositiveButton("Xóa", (dialog, which) -> performDelete(item)) // Xác nhận → xóa
                .setNegativeButton("Hủy", null) // Hủy → đóng dialog, không làm gì
                .show();
    }

    /**
     * Thực hiện xóa khu vực sau khi người dùng đã xác nhận.
     *
     * API: DELETE /api/areas/{id}
     * Server sẽ từ chối (409/400) nếu khu vực còn bàn hoặc đang được sử dụng.
     *
     * @param item Khu vực cần xóa
     */
    private void performDelete(Area item) {
        if (item.getId() == null) return;

        ZappyApiService api = RetrofitClient.getApiService();
        api.deleteArea(item.getId()).enqueue(new Callback<Map>() {
            @Override
            public void onResponse(Call<Map> call, Response<Map> response) {
                if (response.isSuccessful()) {
                    // Xóa thành công → xóa khỏi danh sách hiển thị và tải lại
                    Toast.makeText(AreaManageActivity.this,
                            "Đã xóa khu vực: " + item.getAreaName(), Toast.LENGTH_SHORT).show();
                    if (adapter != null) {
                        adapter.removeItem(item);
                        loadAreas(); // Tải lại để cập nhật thống kê
                    }
                } else if (response.code() == 409 || response.code() == 400) {
                    // 409 Conflict hoặc 400 Bad Request: còn bàn trong khu vực
                    Toast.makeText(AreaManageActivity.this,
                            "Không thể xóa: khu vực đang có bàn hoặc đang được sử dụng",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AreaManageActivity.this,
                            "Xóa thất bại (lỗi " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map> call, Throwable t) {
                Toast.makeText(AreaManageActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Callback: Người dùng bấm vào TÊN khu vực (xem chi tiết).
     *
     * Chuyển sang ChiTietKhuVucActivity để xem danh sách bàn trong khu vực.
     * Truyền toàn bộ object Area qua Intent (Area implements Serializable).
     *
     * @param item Khu vực được bấm vào
     */
    @Override
    public void onItemClick(Area item) {
        Intent intent = new Intent(this, ChiTietKhuVucActivity.class);
        intent.putExtra("AREA_DATA", item); // Truyền object Area (phải Serializable)
        startActivity(intent);
    }

    // ============================================================
    //  BƯỚC 5: ĐIỀU HƯỚNG BOTTOM NAVIGATION
    // ============================================================

    /**
     * Gán sự kiện click cho 3 tab điều hướng dưới cùng.
     */
    private void setupBottomNav() {
        View navOrder   = findViewById(R.id.navOrder);
        View navSoDo    = findViewById(R.id.navSoDo);
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
