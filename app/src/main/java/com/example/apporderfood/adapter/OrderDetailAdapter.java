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

    public interface OnCancelItemListener {
        void onCancelItem(OrderDetail detail);
    }

    private final Context context;
    private List<OrderDetail> items;
    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private final boolean isAdmin;
    private OnCancelItemListener cancelListener;

    /**
     * HÀM KHỞI TẠO ADAPTER
     * @param context Context màn hình
     * @param items Danh sách các món ăn chi tiết
     * @param isAdmin Cờ báo xem người dùng có phải là Quản lý không (để hiện nút Hủy món)
     * @param cancelListener Lắng nghe sự kiện bấm nút Xóa/Hủy
     */
    public OrderDetailAdapter(Context context, List<OrderDetail> items, boolean isAdmin, OnCancelItemListener cancelListener) {
        this.context = context;
        this.items = items;
        this.isAdmin = isAdmin;
        this.cancelListener = cancelListener;
    }

    /**
     * CẬP NHẬT LẠI DANH SÁCH MÓN ĂN VÀ VẼ LẠI GIAO DIỆN
     */
    public void setItems(List<OrderDetail> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    /**
     * TẠO GIAO DIỆN MỚI CHO 1 DÒNG MÓN ĂN
     * Nạp layout từ file item_order_detail.xml
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false);
        return new ViewHolder(view);
    }

    /**
     * ĐỔ DỮ LIỆU CỦA 1 MÓN ĂN LÊN GIAO DIỆN (Tên, Giá, Trạng thái...)
     * Xử lý giao diện cho món bị gạch ngang khi Status = 2 (Đã hủy).
     * Phân quyền hiển thị nút Xóa/Hủy món.
     */
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

       
        if (detail.getStatus() != null && detail.getStatus() == 2) {
            // Món đã hủy
            holder.tvItemName.setPaintFlags(holder.tvItemName.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvItemName.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            holder.tvQtyBadge.setBackgroundResource(R.drawable.bg_badge_light);
            holder.tvQtyBadge.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            holder.btnCancel.setVisibility(View.GONE);
            holder.tvSubTotal.setPaintFlags(holder.tvSubTotal.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvItemName.setPaintFlags(holder.tvItemName.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvItemName.setTextColor(context.getResources().getColor(R.color.text_primary));
            holder.tvQtyBadge.setBackgroundResource(R.drawable.bg_badge_dark);
            holder.tvQtyBadge.setTextColor(context.getResources().getColor(R.color.surface));
            holder.tvSubTotal.setPaintFlags(holder.tvSubTotal.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));

            if (detail.getStatus() != null && detail.getStatus() != 2) {
                if (detail.getStatus() == 0 || isAdmin) {
                    holder.btnCancel.setVisibility(View.VISIBLE);
                    if (detail.getStatus() == 0) {
                        holder.btnCancel.setText("Xóa món");
                    } else {
                        holder.btnCancel.setText("Hủy món");
                    }
                    holder.btnCancel.setOnClickListener(v -> {
                        if (cancelListener != null) cancelListener.onCancelItem(detail);
                    });
                } else {
                    holder.btnCancel.setVisibility(View.GONE);
                }
            } else {
                holder.btnCancel.setVisibility(View.GONE);
            }
        }
    }

    /**
     * TRẢ VỀ TỔNG SỐ LƯỢNG MÓN ĐANG HIỂN THỊ
     */
    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    /**
     * LỚP VIEWHOLDER: ÁNH XẠ GIAO DIỆN XML
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQtyBadge, tvItemName, tvUnitPrice, tvNote, tvSubTotal, btnCancel;
        LinearLayout llNote;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQtyBadge  = itemView.findViewById(R.id.tvQtyBadge);
            tvItemName  = itemView.findViewById(R.id.tvItemName);
            tvUnitPrice = itemView.findViewById(R.id.tvUnitPrice);
            llNote      = itemView.findViewById(R.id.llNote);
            tvNote      = itemView.findViewById(R.id.tvNote);
            tvSubTotal  = itemView.findViewById(R.id.tvSubTotal);
            btnCancel   = itemView.findViewById(R.id.btnCancel);
        }
    }
}
