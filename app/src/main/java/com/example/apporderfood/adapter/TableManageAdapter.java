package com.example.apporderfood.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apporderfood.R;
import com.example.apporderfood.model.TableModel;
import java.util.List;

public class TableManageAdapter extends RecyclerView.Adapter<TableManageAdapter.TableViewHolder> {

    private List<TableModel> tableList;
    private OnTableItemClickListener listener;

    public interface OnTableItemClickListener {
        void onEditClick(TableModel item);
        void onMoreClick(TableModel item, View view);
        void onItemClick(TableModel item);
    }

    public TableManageAdapter(List<TableModel> tableList, OnTableItemClickListener listener) {
        this.tableList = tableList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_table_manage, parent, false);
        return new TableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        TableModel item = tableList.get(position);
        holder.tvTableId.setText(item.getTableName());
        
        String status = item.getStatus() != null ? item.getStatus() : "HOẠT ĐỘNG";
        holder.tvTableStatus.setText(status);
        
        String areaName = item.getArea() != null ? item.getArea().getAreaName() : "Chưa rõ";
        int seats = item.getSeats() != null ? item.getSeats() : 4;
        holder.tvTableInfo.setText(areaName + " • " + seats + " chỗ ngồi");
        
        // Giả lập ngày tạo nếu chưa có trong model
        holder.tvDateCreated.setText("ID: " + item.getId());

        // Update status UI
        if ("HOẠT ĐỘNG".equals(status)) {
            holder.tvTableStatus.setBackgroundResource(R.drawable.bg_status_available);
            holder.tvTableStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        } else if ("ĐANG KHÓA".equals(status)) {
            holder.tvTableStatus.setBackgroundResource(R.drawable.bg_status_unavailable);
            holder.tvTableStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
        } else {
            holder.tvTableStatus.setBackgroundResource(R.drawable.bg_status_paused);
            holder.tvTableStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });

        holder.ivTableEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(item);
        });

        holder.ivTableMore.setOnClickListener(v -> {
            if (listener != null) listener.onMoreClick(item, v);
        });
    }

    @Override
    public int getItemCount() {
        return tableList != null ? tableList.size() : 0;
    }

    public static class TableViewHolder extends RecyclerView.ViewHolder {
        TextView tvTableId, tvTableStatus, tvTableInfo, tvDateCreated;
        ImageView ivTableEdit, ivTableMore;

        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTableId = itemView.findViewById(R.id.tvTableId);
            tvTableStatus = itemView.findViewById(R.id.tvTableStatus);
            tvTableInfo = itemView.findViewById(R.id.tvTableInfo);
            tvDateCreated = itemView.findViewById(R.id.tvDateCreated);
            ivTableEdit = itemView.findViewById(R.id.ivTableEdit);
            ivTableMore = itemView.findViewById(R.id.ivTableMore);
        }
    }
}
