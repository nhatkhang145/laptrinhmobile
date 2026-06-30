package com.example.apporderfood.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apporderfood.R;
import com.example.apporderfood.model.Category;
import java.util.List;

/**
 * CategoryManageAdapter
 * Adapter dùng để hiển thị danh sách danh mục trên RecyclerView.
 * Cung cấp cơ chế gắn kết dữ liệu (bind data) từ đối tượng Category sang các view tương ứng (item_category_manage.xml).
 */
public class CategoryManageAdapter extends RecyclerView.Adapter<CategoryManageAdapter.CategoryViewHolder> {

    private List<Category> categoryList;
    private OnCategoryItemClickListener listener;

    /**
     * Interface định nghĩa các sự kiện tương tác của người dùng trên mỗi item danh mục.
     * Cần được cài đặt (implement) bởi Activity chứa adapter.
     */
    public interface OnCategoryItemClickListener {
        void onEditClick(Category item);      // Xử lý sự kiện khi nhấn nút Chỉnh sửa
        void onDeleteClick(Category item);    // Xử lý sự kiện khi nhấn nút Xóa
        void onStatusToggleClick(Category item, int position); // Xử lý sự kiện khi nhấn đổi trạng thái
        void onItemClick(Category item);      // Xử lý sự kiện khi nhấn vào item (xem chi tiết)
    }

    public CategoryManageAdapter(List<Category> categoryList, OnCategoryItemClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    public void removeItem(Category item) {
        int pos = categoryList.indexOf(item);
        if (pos >= 0) {
            categoryList.remove(pos);
            notifyItemRemoved(pos);
        }
    }

    /** Cập nhật status local và refresh item UI ngay lập tức */
    public void updateItemStatus(int position, int newStatus) {
        if (position < 0 || position >= categoryList.size()) return;
        categoryList.get(position).setStatus(newStatus);
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_manage, parent, false);
        return new CategoryViewHolder(view);
    }

    /**
     * Gắn kết dữ liệu vào ViewHolder cho từng item.
     */
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category item = categoryList.get(position);
        holder.tvCategoryName.setText(item.getCatName());

        // Kiểm tra và hiển thị trạng thái (null hoặc 1 = HOẠT ĐỘNG, 0 = TẠM ẨN)
        boolean active = item.getStatus() == null || item.getStatus() == 1;
        applyStatusStyle(holder.tvCategoryStatus, active);

        // Hiển thị số lượng món ăn và mô tả nếu có
        int count = item.getItemCount() != null ? item.getItemCount() : 0;
        String desc = item.getDescription() != null && !item.getDescription().isEmpty()
                ? " • " + item.getDescription() : "";
        holder.tvCategoryInfo.setText(count + " món" + desc);

        // Đăng ký các sự kiện onClick thông qua listener
        // Nhấn badge trạng thái → toggle ngay
        holder.tvCategoryStatus.setOnClickListener(v -> {
            if (listener != null) listener.onStatusToggleClick(item, holder.getAdapterPosition());
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });

        holder.ivCategoryEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(item);
        });

        holder.ivCategoryDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(item);
        });
    }

    private void applyStatusStyle(TextView tv, boolean active) {
        if (active) {
            tv.setText("● HOẠT ĐỘNG");
            tv.setBackgroundResource(R.drawable.bg_status_available);
            tv.setTextColor(tv.getContext().getColor(android.R.color.holo_green_dark));
        } else {
            tv.setText("○ TẠM ẨN");
            tv.setBackgroundResource(R.drawable.bg_status_paused);
            tv.setTextColor(tv.getContext().getColor(android.R.color.holo_orange_dark));
        }
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvCategoryStatus, tvCategoryInfo;
        ImageView ivCategoryEdit, ivCategoryDelete;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName   = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryStatus = itemView.findViewById(R.id.tvCategoryStatus);
            tvCategoryInfo   = itemView.findViewById(R.id.tvCategoryInfo);
            ivCategoryEdit   = itemView.findViewById(R.id.ivCategoryEdit);
            ivCategoryDelete = itemView.findViewById(R.id.ivCategoryDelete);
        }
    }
}
