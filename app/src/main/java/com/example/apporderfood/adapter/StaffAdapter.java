package com.example.apporderfood.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.Activity.ChiTietNhanVienActivity;
import com.example.apporderfood.R;
import com.example.apporderfood.model.User;

import java.util.ArrayList;
import java.util.List;

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
                // Đảm bảo bạn đã tạo file bg_offline_dot.xml màu xám nhé!
                holder.viewStatusDot.setBackgroundResource(R.drawable.bg_offline_dot);
            }
        }
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChiTietNhanVienActivity.class);
            // Truyền toàn bộ object user sang màn hình Chi tiết
            intent.putExtra("USER_DATA", user);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return staffList != null ? staffList.size() : 0;
    }

    public static class StaffViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvContact, tvRoleTag, tvStatusDetail;
        View viewStatusDot;
        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvRoleTag = itemView.findViewById(R.id.tvRoleTag);
            tvStatusDetail = itemView.findViewById(R.id.tvStatusDetail);
            viewStatusDot = itemView.findViewById(R.id.viewStatusDot);
        }
    }
}