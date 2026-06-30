package com.example.apporderfood.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.Activity.ChiTietNhanVienActivity;
import com.example.apporderfood.R;
import com.example.apporderfood.api.RetrofitClient;
import com.example.apporderfood.api.ZappyApiService;
import com.example.apporderfood.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
/**
 * Adapter hiển thị danh sách nhân viên.
 *
 * Chức năng:
 * - Hiển thị thông tin nhân viên.
 * - Tìm kiếm theo tên, email.
 * - Lọc theo vai trò.
 * - Khóa/Mở khóa tài khoản.
 * - Chuyển sang màn hình chi tiết nhân viên.
 */
public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {

    private Context context;
    private List<User> staffList;
    private List<User> staffListFull; // Dùng để backup dữ liệu gốc khi tìm kiếm
    private String currentKeyword = "";
    private Integer currentRole = null; // null nghĩa là đang hiển thị "Tất cả"

    public StaffAdapter(Context context, List<User> staffList) {
        this.context = context;
        this.staffList = staffList;
        this.staffListFull = new ArrayList<>(staffList);
    }
    /**
     * Cập nhật toàn bộ danh sách nhân viên.
     *
     * Sau khi cập nhật sẽ áp dụng lại bộ lọc hiện tại
     * để giữ nguyên kết quả tìm kiếm hoặc tab đang chọn.
     */
    public void updateData(List<User> newStaffList) {
        this.staffList.clear();
        this.staffList.addAll(newStaffList);
        this.staffListFull.clear();
        this.staffListFull.addAll(newStaffList);

        // Gọi lại bộ lọc để giữ nguyên tab hiện tại khi data mới tải về
        applyFilters();
    }

    // 1. Nhận sự kiện khi gõ vào ô tìm kiếm
    /**
     * Lọc danh sách theo từ khóa tìm kiếm.
     */
    public void filterByText(String text) {
        this.currentKeyword = text.toLowerCase().trim();
        applyFilters();
    }

    // 2. Nhận sự kiện khi bấm vào các Tab Role (Quản lý, Phục vụ...)
    /**
     * Lọc danh sách theo vai trò nhân viên.
     *
     * role:
     * - null : Tất cả
     * - 1 : Quản lý
     * - 2 : Thu ngân
     * - 0 : Nhân viên
     */
    public void filterByRole(Integer role) {
        this.currentRole = role;
        applyFilters();
    }

    // 3. Hàm xử lý logic lọc kép
    /**
     * Áp dụng đồng thời bộ lọc theo từ khóa và vai trò.
     *
     * Chỉ những nhân viên thỏa mãn cả hai điều kiện
     * mới được hiển thị trong RecyclerView.
     */
    private void applyFilters() {
        staffList.clear();
        for (User user : staffListFull) {
            // Kiểm tra khớp từ khóa
            boolean matchesText = currentKeyword.isEmpty() ||
                    (user.getUsername() != null && user.getUsername().toLowerCase().contains(currentKeyword)) ||
                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(currentKeyword)) ||
                    (user.getFullname() != null && user.getFullname().toLowerCase().contains(currentKeyword));

            // Kiểm tra khớp role
            boolean matchesRole = currentRole == null || (user.getRole() != null && user.getRole().equals(currentRole));

            // Thỏa mãn cả 2 thì hiển thị
            if (matchesText && matchesRole) {
                staffList.add(user);
            }
        }
        notifyDataSetChanged();
    }
    /**
     * Tạo View cho một nhân viên trong RecyclerView.
     */
    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_nhan_vien, parent, false);
        return new StaffViewHolder(view);
    }
    /**
     * Hiển thị dữ liệu của một nhân viên lên giao diện.
     *
     * Bao gồm:
     * - Thông tin cá nhân.
     * - Vai trò.
     * - Trạng thái hoạt động.
     * - Xử lý xem chi tiết.
     * - Khóa/Mở khóa tài khoản.
     */
    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        User user = staffList.get(position);
        // Ưu tiên hiển thị họ tên, nếu chưa có thì hiển thị username
        if (user.getFullname() != null && !user.getFullname().trim().isEmpty()) {
            holder.tvName.setText(user.getFullname());
        } else {
            holder.tvName.setText(user.getUsername());
        }
        holder.tvContact.setText(user.getEmail());
        // Hiển thị tên vai trò và màu sắc tương ứng
        if (user.getRole() != null) {
            if (user.getRole() == 1) {
                holder.tvRoleTag.setText("QUẢN LÝ");
                holder.tvRoleTag.setBackgroundColor(Color.parseColor("#EF4444")); // Màu Đỏ
            } else if (user.getRole() == 2) {
                holder.tvRoleTag.setText("THU NGÂN");
                holder.tvRoleTag.setBackgroundColor(Color.parseColor("#3B82F6")); // Màu Cam
            } else {
                holder.tvRoleTag.setText("NHÂN VIÊN");
                holder.tvRoleTag.setBackgroundColor(Color.parseColor("#F59E0B")); // Màu Xanh dương
            }
        } else {
            // Mặc định
            holder.tvRoleTag.setText("NHÂN VIÊN");
            holder.tvRoleTag.setBackgroundColor(Color.parseColor("#F59E0B"));
        }
        // Hiển thị trạng thái tài khoản:
        // - Bị khóa
        // - Trực tuyến
        // - Ngoại tuyến
        Boolean isActive = user.getIsActive();
        Boolean isOnline = user.getIsOnline();
        if (isActive != null && !isActive) {
            holder.tvStatusDetail.setText("Bị khóa");
            holder.tvStatusDetail.setTextColor(Color.parseColor("#EF4444"));
            holder.viewStatusDot.setBackgroundResource(R.drawable.bg_icon_red); // Chấm đỏ
        }
        else {
            if (isOnline != null && isOnline) {
                holder.tvStatusDetail.setText("Trực tuyến");
                holder.tvStatusDetail.setTextColor(Color.parseColor("#10B981"));
                holder.viewStatusDot.setBackgroundResource(R.drawable.bg_online_dot);
            } else {
                holder.tvStatusDetail.setText("Ngoại tuyến");
                holder.tvStatusDetail.setTextColor(Color.parseColor("#9CA3AF")); // Xám
                holder.viewStatusDot.setBackgroundResource(R.drawable.bg_offline_dot);
            }
        }
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChiTietNhanVienActivity.class);
            // Truyền toàn bộ object user sang màn hình Chi tiết
            intent.putExtra("USER_DATA", user);
            context.startActivity(intent);
        });
        boolean isCurrentlyActive = (isActive == null || isActive);
        if (isCurrentlyActive) {
            holder.btnLock.setImageResource(R.drawable.ic_lock);
            holder.btnLock.setColorFilter(Color.parseColor("#EF4444"));

        } else {
            holder.btnLock.setImageResource(R.drawable.ic_lock_reset_nv);
            holder.btnLock.setColorFilter(Color.parseColor("#10B981"));
        }

        // Đổi biểu tượng nút theo trạng thái tài khoản
        // Xử lý khóa hoặc mở khóa tài khoản nhân viên
        holder.btnLock.setOnClickListener(v -> {
            String actionName = isCurrentlyActive ? "khóa" : "mở khóa";
            // Hiển thị hộp thoại xác nhận
            new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle(isCurrentlyActive ? "Khóa tài khoản" : "Mở khóa tài khoản")
                    .setMessage("Bạn có chắc muốn " + actionName + " tài khoản của " + (user.getFullname() != null ? user.getFullname() : user.getUsername()) + " không?")
                    .setPositiveButton("Đồng ý", (dialog, which) -> {
                        // Gọi API cập nhật trạng thái
                        ZappyApiService api = RetrofitClient.getApiService();
                        api.toggleLockUser(user.getId()).enqueue(new retrofit2.Callback<Map<String, Boolean>>() {
                            @Override
                            public void onResponse(Call<Map<String, Boolean>> call,Response<Map<String, Boolean>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    // Cập nhật trạng thái tài khoản trong danh sách
                                    Boolean newStatus = response.body().get("isActive");
                                    user.setIsActive(newStatus);
                                    int adapterPosition = holder.getAdapterPosition();
                                    if (adapterPosition != RecyclerView.NO_POSITION) {
                                        notifyItemChanged(adapterPosition);
                                    }
                                    Toast.makeText(context,"Đã " + actionName + " tài khoản!",Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<Map<String, Boolean>> call,Throwable t) {
                                Toast.makeText(context,"Lỗi mạng!",Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }
    /**
     * Trả về số lượng nhân viên đang hiển thị.
     */
    @Override
    public int getItemCount() {
        return staffList != null ? staffList.size() : 0;
    }
    /**
     * ViewHolder lưu tham chiếu đến các View
     * để tăng hiệu năng của RecyclerView.
     */
    public static class StaffViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvContact, tvRoleTag, tvStatusDetail;
        View viewStatusDot;
        ImageView btnLock ;
        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvRoleTag = itemView.findViewById(R.id.tvRoleTag);
            tvStatusDetail = itemView.findViewById(R.id.tvStatusDetail);
            viewStatusDot = itemView.findViewById(R.id.viewStatusDot);
            btnLock= itemView.findViewById(R.id.btnLock);
        }
    }
}