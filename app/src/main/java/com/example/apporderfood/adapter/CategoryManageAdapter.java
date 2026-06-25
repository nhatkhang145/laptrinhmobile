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
        void onMoreClick(Category item, View view);
        void onItemClick(Category item);
    }

    public CategoryManageAdapter(List<Category> categoryList, OnCategoryItemClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_manage, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category item = categoryList.get(position);
        holder.tvCategoryName.setText(item.getCatName());
        
        String statusText = (item.getStatus() != null && item.getStatus() == 1) ? "HOẠT ĐỘNG" : "TẠM ẨN";
        holder.tvCategoryStatus.setText(statusText);
        
        int count = item.getItemCount() != null ? item.getItemCount() : 0;
        holder.tvCategoryInfo.setText(count + " món • " + (item.getDescription() != null ? item.getDescription() : ""));

        if ("HOẠT ĐỘNG".equals(statusText)) {
            holder.tvCategoryStatus.setBackgroundResource(R.drawable.bg_status_available);
            holder.tvCategoryStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvCategoryStatus.setBackgroundResource(R.drawable.bg_status_paused);
            holder.tvCategoryStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });

        holder.ivCategoryEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(item);
        });

        holder.ivCategoryMore.setOnClickListener(v -> {
            if (listener != null) listener.onMoreClick(item, v);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvCategoryStatus, tvCategoryInfo;
        ImageView ivCategoryEdit, ivCategoryMore;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryStatus = itemView.findViewById(R.id.tvCategoryStatus);
            tvCategoryInfo = itemView.findViewById(R.id.tvCategoryInfo);
            ivCategoryEdit = itemView.findViewById(R.id.ivCategoryEdit);
            ivCategoryMore = itemView.findViewById(R.id.ivCategoryMore);
        }
    }
}
