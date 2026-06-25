package com.example.apporderfood.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apporderfood.R;
import java.util.List;

public class CategoryManageAdapter extends RecyclerView.Adapter<CategoryManageAdapter.CategoryViewHolder> {

    private List<CategoryItem> categoryList;

    public CategoryManageAdapter(List<CategoryItem> categoryList) {
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_manage, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryItem item = categoryList.get(position);
        holder.tvCategoryName.setText(item.getName());
        holder.tvCategoryStatus.setText(item.getStatus());
        holder.tvCategoryInfo.setText(item.getItemCount() + " món • " + item.getDescription());

        if ("HOẠT ĐỘNG".equals(item.getStatus())) {
            holder.tvCategoryStatus.setBackgroundResource(R.drawable.bg_status_available);
            holder.tvCategoryStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvCategoryStatus.setBackgroundResource(R.drawable.bg_status_paused);
            holder.tvCategoryStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
        }
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvCategoryStatus, tvCategoryInfo;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryStatus = itemView.findViewById(R.id.tvCategoryStatus);
            tvCategoryInfo = itemView.findViewById(R.id.tvCategoryInfo);
        }
    }

    public static class CategoryItem {
        private String name;
        private String status;
        private int itemCount;
        private String description;

        public CategoryItem(String name, String status, int itemCount, String description) {
            this.name = name;
            this.status = status;
            this.itemCount = itemCount;
            this.description = description;
        }

        public String getName() { return name; }
        public String getStatus() { return status; }
        public int getItemCount() { return itemCount; }
        public String getDescription() { return description; }
    }
}
