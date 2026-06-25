package com.example.apporderfood.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.R;
import com.example.apporderfood.model.User;

import java.util.ArrayList;
import java.util.List;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {

    private Context context;
    private List<User> staffList;
    private List<User> staffListFull; // Dùng để backup dữ liệu gốc khi tìm kiếm

    public StaffAdapter(Context context, List<User> staffList) {
        this.context = context;
        this.staffList = staffList;
        this.staffListFull = new ArrayList<>(staffList);
    }

    // Hàm cập nhật lại danh sách khi API gọi xong
    public void updateData(List<User> newStaffList) {
        this.staffList.clear();
        this.staffList.addAll(newStaffList);
        this.staffListFull.clear();
        this.staffListFull.addAll(newStaffList);
        notifyDataSetChanged();
    }

    // Hàm lọc danh sách theo từ khóa tìm kiếm
    public void filter(String text) {
        staffList.clear();
        if (text.isEmpty()) {
            staffList.addAll(staffListFull);
        } else {
            text = text.toLowerCase();
            for (User user : staffListFull) {
                if (user.getUsername().toLowerCase().contains(text) ||
                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(text))) {
                    staffList.add(user);
                }
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

        holder.tvName.setText(user.getUsername());

        // Hiển thị email nếu có, không có thì để trống
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            holder.tvContact.setText(user.getEmail());
        } else {
            holder.tvContact.setText("Chưa cập nhật");
        }

        // Xử lý hiển thị Tag (1 = Quản lý, 0 = Nhân viên)
        if (user.isManager()) {
            holder.tvRoleTag.setText("QUẢN LÝ");
            holder.tvRoleTag.setBackgroundColor(Color.parseColor("#EF4444")); // Màu đỏ
        } else {
            holder.tvRoleTag.setText("NHÂN VIÊN");
            holder.tvRoleTag.setBackgroundColor(Color.parseColor("#3B82F6")); // Màu xanh lam
        }

        // TODO: Xử lý sự kiện click vào nút Edit/More ở đây
    }

    @Override
    public int getItemCount() {
        return staffList != null ? staffList.size() : 0;
    }

    public static class StaffViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvContact, tvRoleTag, tvStatusDetail;

        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvRoleTag = itemView.findViewById(R.id.tvRoleTag);
            tvStatusDetail = itemView.findViewById(R.id.tvStatusDetail);
        }
    }
}