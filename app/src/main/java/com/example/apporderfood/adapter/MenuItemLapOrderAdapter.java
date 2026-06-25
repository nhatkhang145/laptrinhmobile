package com.example.apporderfood.adapter;

import android.content.Context;
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
import com.mikepenz.iconics.view.IconicsImageView;

import java.text.DecimalFormat;
import java.util.List;

public class MenuItemLapOrderAdapter extends RecyclerView.Adapter<MenuItemLapOrderAdapter.ViewHolder> {

    private Context context;
    private List<MenuItem> menuItems;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onAddClick(MenuItem item);
    }

    public MenuItemLapOrderAdapter(Context context, List<MenuItem> menuItems, OnItemClickListener listener) {
        this.context = context;
        this.menuItems = menuItems;
        this.listener = listener;
    }

    public void setMenuItems(List<MenuItem> items) {
        this.menuItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_menu_lap_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuItem item = menuItems.get(position);

        holder.tvFoodName.setText(item.getItemName() != null ? item.getItemName() : "");
        
        // Mo ta mon an - tam thoi de trong hoac hien thi unit name
        if (item.getUnit() != null) {
            holder.tvFoodDesc.setText("ĐVT: " + item.getUnit().getUnitName());
        } else {
            holder.tvFoodDesc.setText("");
        }

        if (item.getPrice() != null) {
            DecimalFormat formatter = new DecimalFormat("#,###");
            holder.tvFoodPrice.setText(formatter.format(item.getPrice()));
        } else {
            holder.tvFoodPrice.setText("0");
        }

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.bg_food_image) // using color hint background
                    .into(holder.ivFoodImage);
        } else {
            holder.ivFoodImage.setImageResource(0);
        }

        holder.btnAddItem.setOnClickListener(v -> listener.onAddClick(item));
    }

    @Override
    public int getItemCount() {
        return menuItems == null ? 0 : menuItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivFoodImage;
        TextView tvFoodName, tvFoodDesc, tvFoodPrice;
        IconicsImageView btnAddItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodDesc = itemView.findViewById(R.id.tvFoodDesc);
            tvFoodPrice = itemView.findViewById(R.id.tvFoodPrice);
            btnAddItem = itemView.findViewById(R.id.btnAddItem);
        }
    }
}
