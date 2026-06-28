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
    private List<TableModel> fullList;
    private OnTableItemClickListener listener;

    public interface OnTableItemClickListener {
        void onEditClick(TableModel item);
        void onDeleteClick(TableModel item);
        void onStatusToggleClick(TableModel item, int position);
        void onItemClick(TableModel item);
    }

    public TableManageAdapter(List<TableModel> tableList, OnTableItemClickListener listener) {
        this.tableList = tableList;
        this.fullList = tableList;
        this.listener = listener;
    }

    public void filter(String query, Integer areaId) {
        String q = (query == null) ? "" : query.toLowerCase().trim();
        java.util.List<TableModel> filtered = new java.util.ArrayList<>();

        for (TableModel t : fullList) {
            boolean matchesQuery = q.isEmpty() || (t.getTableName() != null && t.getTableName().toLowerCase().contains(q));
            boolean matchesArea = (areaId == null) || (t.getArea() != null && t.getArea().getId() != null && t.getArea().getId().equals(areaId));
            
            if (matchesQuery && matchesArea) {
                filtered.add(t);
            }
        }
        tableList = filtered;
        notifyDataSetChanged();
    }

    public List<TableModel> getTableList() {
        return tableList;
    }

    public void removeItem(TableModel item) {
        int pos = tableList.indexOf(item);
        if (pos >= 0) {
            tableList.remove(pos);
            notifyItemRemoved(pos);
        }
    }

    public void updateItemStatus(int position, String newStatus) {
        if (position >= 0 && position < tableList.size()) {
            tableList.get(position).setStatus(newStatus);
            notifyItemChanged(position);
        }
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
            holder.tvTableStatus.setText("● HOẠT ĐỘNG");
            holder.tvTableStatus.setBackgroundResource(R.drawable.bg_status_available);
            holder.tvTableStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvTableStatus.setText("○ ĐANG KHÓA");
            holder.tvTableStatus.setBackgroundResource(R.drawable.bg_status_paused);
            holder.tvTableStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
        }

        holder.tvTableStatus.setOnClickListener(v -> {
            if (listener != null) listener.onStatusToggleClick(item, holder.getAdapterPosition());
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });

        holder.ivTableEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(item);
        });

        holder.ivTableDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return tableList != null ? tableList.size() : 0;
    }

    public static class TableViewHolder extends RecyclerView.ViewHolder {
        TextView tvTableId, tvTableStatus, tvTableInfo, tvDateCreated;
        ImageView ivTableEdit, ivTableDelete;

        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTableId = itemView.findViewById(R.id.tvTableId);
            tvTableStatus = itemView.findViewById(R.id.tvTableStatus);
            tvTableInfo = itemView.findViewById(R.id.tvTableInfo);
            tvDateCreated = itemView.findViewById(R.id.tvDateCreated);
            ivTableEdit = itemView.findViewById(R.id.ivTableEdit);
            ivTableDelete = itemView.findViewById(R.id.ivTableDelete);
        }
    }
}
