package com.example.apporderfood.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apporderfood.R;
import java.util.List;

public class TableManageAdapter extends RecyclerView.Adapter<TableManageAdapter.TableViewHolder> {

    private List<TableItem> tableList;

    public TableManageAdapter(List<TableItem> tableList) {
        this.tableList = tableList;
    }

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_table_manage, parent, false);
        return new TableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        TableItem item = tableList.get(position);
        holder.tvTableId.setText(item.getTableId());
        holder.tvTableStatus.setText(item.getStatus());
        holder.tvTableInfo.setText(item.getArea() + " • " + item.getSeats() + " chỗ ngồi");
        holder.tvDateCreated.setText("Ngày tạo: " + item.getDateCreated());

        // Update status UI
        if ("HOẠT ĐỘNG".equals(item.getStatus())) {
            holder.tvTableStatus.setBackgroundResource(R.drawable.bg_status_available);
            holder.tvTableStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        } else if ("ĐANG KHÓA".equals(item.getStatus())) {
            holder.tvTableStatus.setBackgroundResource(R.drawable.bg_status_unavailable);
            holder.tvTableStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
        } else {
            holder.tvTableStatus.setBackgroundResource(R.drawable.bg_status_paused);
            holder.tvTableStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
        }
    }

    @Override
    public int getItemCount() {
        return tableList.size();
    }

    public static class TableViewHolder extends RecyclerView.ViewHolder {
        TextView tvTableId, tvTableStatus, tvTableInfo, tvDateCreated;

        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTableId = itemView.findViewById(R.id.tvTableId);
            tvTableStatus = itemView.findViewById(R.id.tvTableStatus);
            tvTableInfo = itemView.findViewById(R.id.tvTableInfo);
            tvDateCreated = itemView.findViewById(R.id.tvDateCreated);
        }
    }

    public static class TableItem {
        private String tableId;
        private String status;
        private String area;
        private int seats;
        private String dateCreated;

        public TableItem(String tableId, String status, String area, int seats, String dateCreated) {
            this.tableId = tableId;
            this.status = status;
            this.area = area;
            this.seats = seats;
            this.dateCreated = dateCreated;
        }

        public String getTableId() { return tableId; }
        public String getStatus() { return status; }
        public String getArea() { return area; }
        public int getSeats() { return seats; }
        public String getDateCreated() { return dateCreated; }
    }
}
