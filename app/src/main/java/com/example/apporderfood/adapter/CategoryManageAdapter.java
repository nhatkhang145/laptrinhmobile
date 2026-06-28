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

public class CategoryManageAdapter extends RecyclerView.Adapter<CategoryManageAdapter.CategoryViewHolder> {

    private List<Category> categoryList;
    private OnCategoryItemClickListener listener;

    public interface OnCategoryItemClickListener {
        void onEditClick(Category item);
        void onDeleteClick(Category item);
        void onStatusToggleClick(Category item, int position);
        void onItemClick(Category item);
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

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category item = categoryList.get(position);
        holder.tvCategoryName.setText(item.getCatName());

        // null hoặc 1 = HOẠT ĐỘNG
        boolean active = item.getStatus() == null || item.getStatus() == 1;
        applyStatusStyle(holder.tvCategoryStatus, active);

        int count = item.getItemCount() != null ? item.getItemCount() : 0;
        String desc = item.getDescription() != null && !item.getDescription().isEmpty()
                ? " • " + item.getDescription() : "";
        holder.tvCategoryInfo.setText(count + " món" + desc);

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
