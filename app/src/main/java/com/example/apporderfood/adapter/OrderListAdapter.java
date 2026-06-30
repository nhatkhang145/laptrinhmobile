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
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.ArrayList;
import java.util.List;

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.OrderViewHolder> {

    private final Context context;
    private List<java.util.Map<String, Object>> orderList = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(java.util.Map<String, Object> orderMap);
    }

    public OrderListAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    /**
     * CẬP NHẬT DANH SÁCH ORDER VÀ VẼ LẠI GIAO DIỆN
     */
    public void setOrderList(List<java.util.Map<String, Object>> list) {
        this.orderList = list;
        notifyDataSetChanged();
    }

    /**
     * TẠO GIAO DIỆN CHO 1 THẺ ORDER
     * Nạp layout từ file item_order_card.xml
     */
    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_card, parent, false);
        return new OrderViewHolder(view);
    }

    /**
     * ĐỔ DỮ LIỆU CỦA 1 ORDER LÊN GIAO DIỆN THẺ (Tên bàn, Số tiền, Thời gian chờ...)
     */
    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        java.util.Map<String, Object> order = orderList.get(position);
        java.util.Map<String, Object> table = (java.util.Map<String, Object>) order.get("table");

        // Hiển thị tên khu vực và bàn
        String areaName = "Không rõ";
        if (table != null && table.get("area") != null) {
            java.util.Map<String, Object> area = (java.util.Map<String, Object>) table.get("area");
            areaName = (String) area.get("areaName");
        }
        String tableName = table != null ? (String) table.get("tableName") : "Bàn";
        holder.tvTableName.setText(areaName + " - " + tableName);

        holder.tvStatus.setText("Đang phục vụ");
        holder.tvStatus.setTextColor(Color.parseColor("#EF4444")); // Red
        holder.dotStatus.setBackgroundResource(R.drawable.bg_dot_red);

        // Hiển thị tổng tiền
        double amount = 0;
        if (order.get("totalAmount") != null) {
            amount = ((Number) order.get("totalAmount")).doubleValue();
        }
        java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");
        holder.tvAmount.setText(formatter.format(amount) + "đ");

        // Hiển thị thời gian (tính từ lúc mở bàn đến hiện tại)
        holder.layoutTime.setVisibility(View.VISIBLE);
        if (order.get("createdAt") != null) {
            String createdAtStr = (String) order.get("createdAt");
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    java.time.LocalDateTime createdAt = java.time.LocalDateTime.parse(createdAtStr);
                    long minutes = java.time.temporal.ChronoUnit.MINUTES.between(createdAt,
                            java.time.LocalDateTime.now());
                    if (minutes < 60) {
                        holder.tvTime.setText(minutes + " phút");
                    } else {
                        long hours = minutes / 60;
                        long mins = minutes % 60;
                        holder.tvTime.setText(hours + "h " + mins + "p");
                    }
                } else {
                    holder.tvTime.setText("-");
                }
            } catch (Exception e) {
                holder.tvTime.setText("-");
            }
        } else {
            holder.tvTime.setText("0 phút");
        }

        holder.iconAction.setIcon(new com.mikepenz.iconics.IconicsDrawable(context,
                com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome.Icon.faw_file_alt));
        holder.tvAction.setText("Chi tiết");
        holder.btnChiTiet.setBackgroundResource(R.drawable.bg_btn_detail);

        holder.btnChiTiet.setOnClickListener(v -> listener.onItemClick(order));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(order));
    }

    /**
     * TRẢ VỀ TỔNG SỐ LƯỢNG ORDER ĐANG CÓ
     */
    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    /**
     * LỚP ORDERVIEWHOLDER: ÁNH XẠ CÁC THÀNH PHẦN GIAO DIỆN TRONG THẺ ORDER
     */
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
