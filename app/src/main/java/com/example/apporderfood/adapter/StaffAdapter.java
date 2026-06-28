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

    public void updateData(List<User> newStaffList) {
        this.staffList.clear();
        this.staffList.addAll(newStaffList);
        this.staffListFull.clear();
        this.staffListFull.addAll(newStaffList);

        // Gọi lại bộ lọc để giữ nguyên tab hiện tại khi data mới tải về
        applyFilters();
    }

    // 1. Nhận sự kiện khi gõ vào ô tìm kiếm
    public void filterByText(String text) {
        this.currentKeyword = text.toLowerCase().trim();
        applyFilters();
    }

    // 2. Nhận sự kiện khi bấm vào các Tab Role (Quản lý, Phục vụ...)
    public void filterByRole(Integer role) {
        this.currentRole = role;
        applyFilters();
    }

    // 3. Hàm xử lý logic lọc kép
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

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_nhan_vien, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        User user = staffList.get(position);

        if (user.getFullname() != null && !user.getFullname().trim().isEmpty()) {
            holder.tvName.setText(user.getFullname());
        } else {
            holder.tvName.setText(user.getUsername());
        }
        holder.tvContact.setText(user.getEmail());
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
        Boolean isActive = user.getIsActive();
        Boolean isOnline = user.getIsOnline();
        if (isActive != null && !isActive) {
            holder.tvStatusDetail.setText("Bị khóa");
            holder.tvStatusDetail.setTextColor(Color.parseColor("#EF4444")); // Đỏ nguy hiểm
            holder.viewStatusDot.setBackgroundResource(R.drawable.bg_icon_red); // Chấm đỏ
        }
        else {
            if (isOnline != null && isOnline) {
                holder.tvStatusDetail.setText("Trực tuyến");
                holder.tvStatusDetail.setTextColor(Color.parseColor("#10B981")); // Xanh lá
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

        // 2. Bắt sự kiện bấm nút ổ khóa
        holder.btnLock.setOnClickListener(v -> {
            String actionName = isCurrentlyActive ? "khóa" : "mở khóa";

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

    @Override
    public int getItemCount() {
        return staffList != null ? staffList.size() : 0;
    }

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