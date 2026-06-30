package com.example.apporderfood.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.apporderfood.R;
import com.example.apporderfood.model.MenuItem;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FoodInCategoryAdapter extends RecyclerView.Adapter<FoodInCategoryAdapter.ViewHolder> {

    private List<MenuItem> items;

    public FoodInCategoryAdapter(List<MenuItem> items) {
        this.items = items;
    }

    public void setItems(List<MenuItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_in_category, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuItem item = items.get(position);

        holder.tvFoodName.setText(item.getItemName());

        // Giá
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        String price = item.getPrice() != null ? fmt.format(item.getPrice()) + "đ" : "—";
        holder.tvFoodPrice.setText(price);

        // Trạng thái
        boolean available = item.getIsAvailable() == null || item.getIsAvailable();
        if (available) {
            holder.tvFoodStatus.setText("CÒN MÓN");
            holder.tvFoodStatus.setBackgroundResource(R.drawable.bg_status_available);
            holder.tvFoodStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvFoodStatus.setText("HẾT MÓN");
            holder.tvFoodStatus.setBackgroundResource(R.drawable.bg_status_paused);
            holder.tvFoodStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
        }

        // Ảnh
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.bg_food_image)
                    .into(holder.ivFoodImage);
        } else {
            holder.ivFoodImage.setImageResource(R.drawable.bg_food_image);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivFoodImage;
        TextView tvFoodName, tvFoodPrice, tvFoodStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodPrice = itemView.findViewById(R.id.tvFoodPrice);
            tvFoodStatus = itemView.findViewById(R.id.tvFoodStatus);
        }
    }
}
