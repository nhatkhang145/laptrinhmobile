package com.example.apporderfood.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.R;
import com.example.apporderfood.model.TableModel;
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.ArrayList;
import java.util.List;

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.OrderViewHolder> {

    private final Context context;
    private List<TableModel> tableList = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(TableModel table);
    }

    public OrderListAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setTableList(List<TableModel> list) {
        this.tableList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_card, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        TableModel table = tableList.get(position);
        
        // Hien thi ten khu vuc va ban
        String areaName = (table.getArea() != null) ? table.getArea().getAreaName() : "Không rõ";
        holder.tvTableName.setText(areaName + " - " + table.getTableName());

        if (table.isOccupied()) {
            holder.tvStatus.setText("Đang phục vụ");
            holder.tvStatus.setTextColor(Color.parseColor("#EF4444")); // Red
            holder.dotStatus.setBackgroundResource(R.drawable.bg_dot_red);
            holder.tvAmount.setText("Đang tính..."); // Truoc mat chua tinh dc tong tien tu API nay
            holder.layoutTime.setVisibility(View.VISIBLE);
            
            holder.iconAction.setIcon(new com.mikepenz.iconics.IconicsDrawable(context, com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome.Icon.faw_file_alt));
            holder.tvAction.setText("Chi tiết");
            holder.btnChiTiet.setBackgroundResource(R.drawable.bg_btn_detail);
        } else {
            holder.tvStatus.setText("Trống");
            holder.tvStatus.setTextColor(Color.parseColor("#10B981")); // Green
            holder.dotStatus.setBackgroundResource(R.drawable.bg_dot_green);
            holder.tvAmount.setText("0đ");
            holder.layoutTime.setVisibility(View.GONE);
            
            holder.iconAction.setIcon(new com.mikepenz.iconics.IconicsDrawable(context, com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome.Icon.faw_plus_circle));
            holder.tvAction.setText("Mở bàn");
            holder.btnChiTiet.setBackgroundResource(R.drawable.bg_btn_outline);
        }

        holder.btnChiTiet.setOnClickListener(v -> listener.onItemClick(table));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(table));
    }

    @Override
    public int getItemCount() {
        return tableList != null ? tableList.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTableName, tvAmount, tvStatus, tvTime, tvAction;
        View dotStatus;
        LinearLayout layoutTime, btnChiTiet;
        IconicsImageView iconAction;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTableName = itemView.findViewById(R.id.tvTableName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            dotStatus = itemView.findViewById(R.id.dotStatus);
            layoutTime = itemView.findViewById(R.id.layoutTime);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnChiTiet = itemView.findViewById(R.id.btnChiTiet);
            iconAction = itemView.findViewById(R.id.iconAction);
            tvAction = itemView.findViewById(R.id.tvAction);
        }
    }
}
