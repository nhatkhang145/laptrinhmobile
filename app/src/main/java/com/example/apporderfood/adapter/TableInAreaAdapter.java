package com.example.apporderfood.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.R;
import com.example.apporderfood.model.TableModel;

import java.util.List;

public class TableInAreaAdapter extends RecyclerView.Adapter<TableInAreaAdapter.ViewHolder> {

    private List<TableModel> items;

    public TableInAreaAdapter(List<TableModel> items) {
        this.items = items;
    }

    public void setItems(List<TableModel> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_table_in_area, parent, false);
        return new ViewHolder(v);
    }

    // Hiển thị dữ liệu lên view
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TableModel item = items.get(position);

        holder.tvTableName.setText(item.getTableName());

        int seats = item.getSeats() != null ? item.getSeats() : 0;
        holder.tvTableSeats.setText("Số ghế: " + seats);

        String status     = item.getStatus();
        boolean locked    = "ĐANG KHÓA".equals(status);
        boolean isOccupied = item.isOccupied();

        // Ktra trạng thái bàn hiển thị màu tương ứng
        if (locked) {
            holder.tvTableStatus.setText("ĐANG KHÓA");
            holder.tvTableStatus.setBackgroundResource(R.drawable.bg_status_paused);
            holder.tvTableStatus.setTextColor(
                    holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));

        } else if (isOccupied) {
            holder.tvTableStatus.setText("ĐANG CÓ KHÁCH");
            holder.tvTableStatus.setBackgroundResource(R.drawable.bg_status_unavailable);
            holder.tvTableStatus.setTextColor(
                    holder.itemView.getContext().getColor(R.color.white));

        } else {
            holder.tvTableStatus.setText("ĐANG TRỐNG");
            holder.tvTableStatus.setBackgroundResource(R.drawable.bg_status_available);
            holder.tvTableStatus.setTextColor(
                    holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    // Class quản lý các view của item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTableName;
        TextView tvTableStatus;
        TextView tvTableSeats;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTableName   = itemView.findViewById(R.id.tvTableName);
            tvTableStatus = itemView.findViewById(R.id.tvTableStatus);
            tvTableSeats  = itemView.findViewById(R.id.tvTableSeats);
        }
    }
}
