package com.example.apporderfood.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.R;
import com.example.apporderfood.model.OrderDetail;

import java.text.DecimalFormat;
import java.util.List;

/**
 * OrderDetailAdapter - Hiển thị danh sách món trong màn hình Xác Nhận Order (XacNhanOrderActivity).
 * Mỗi item hiển thị: số lượng, tên món, đơn giá, ghi chú, thành tiền.
 */
public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {

    private final Context context;
    private List<OrderDetail> items;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public OrderDetailAdapter(Context context, List<OrderDetail> items) {
        this.context = context;
        this.items = items;
    }

    public void setItems(List<OrderDetail> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDetail detail = items.get(position);

        // Số lượng
        int qty = detail.getQuantity() != null ? detail.getQuantity() : 0;
        holder.tvQtyBadge.setText(String.valueOf(qty));

        // Tên món
        String name = "";
        if (detail.getMenuItem() != null && detail.getMenuItem().getItemName() != null) {
            name = detail.getMenuItem().getItemName();
        }
        holder.tvItemName.setText(name);

        // Đơn giá
        if (detail.getPriceAtSale() != null) {
            String unitName = "";
            if (detail.getMenuItem() != null && detail.getMenuItem().getUnit() != null) {
                unitName = " / " + detail.getMenuItem().getUnit().getUnitName();
            }
            holder.tvUnitPrice.setText(formatter.format(detail.getPriceAtSale()) + "đ" + unitName);
        } else {
            holder.tvUnitPrice.setText("---");
        }

        // Ghi chú
        String note = detail.getNote();
        if (note != null && !note.trim().isEmpty()) {
            holder.llNote.setVisibility(View.VISIBLE);
            holder.tvNote.setText("Ghi chú: " + note);
        } else {
            holder.llNote.setVisibility(View.GONE);
        }

        // Thành tiền
        if (detail.getSubTotal() != null) {
            holder.tvSubTotal.setText(formatter.format(detail.getSubTotal()));
        } else {
            holder.tvSubTotal.setText("0");
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQtyBadge, tvItemName, tvUnitPrice, tvNote, tvSubTotal;
        LinearLayout llNote;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQtyBadge  = itemView.findViewById(R.id.tvQtyBadge);
            tvItemName  = itemView.findViewById(R.id.tvItemName);
            tvUnitPrice = itemView.findViewById(R.id.tvUnitPrice);
            llNote      = itemView.findViewById(R.id.llNote);
            tvNote      = itemView.findViewById(R.id.tvNote);
            tvSubTotal  = itemView.findViewById(R.id.tvSubTotal);
        }
    }
}
