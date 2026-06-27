package com.example.apporderfood.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InvoiceManageAdapter extends RecyclerView.Adapter<InvoiceManageAdapter.ViewHolder> {

    private Context context;
    private List<Map<String, Object>> invoices;
    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Map<String, Object> invoiceMap);
    }

    public InvoiceManageAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.invoices = new ArrayList<>();
    }

    public void setInvoices(List<Map<String, Object>> invoices) {
        this.invoices = invoices;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_invoice_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> invoice = invoices.get(position);

        if (invoice.get("id") != null) {
            holder.tvOrderId.setText("#" + ((Number) invoice.get("id")).intValue());
        }

        Object createdAtObj = invoice.get("createdAt");
        if (createdAtObj != null) {
            if (createdAtObj instanceof String) {
                String createdAt = (String) createdAtObj;
                try {
                    SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    java.util.Date date = parser.parse(createdAt);
                    SimpleDateFormat printer = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
                    holder.tvOrderTime.setText(printer.format(date));
                } catch (Exception e) {
                    holder.tvOrderTime.setText(createdAt);
                }
            } else if (createdAtObj instanceof List) {
                try {
                    List<Number> list = (List<Number>) createdAtObj;
                    if (list.size() >= 5) {
                        String formatted = String.format(Locale.getDefault(), "%02d/%02d/%04d - %02d:%02d",
                                list.get(2).intValue(), list.get(1).intValue(), list.get(0).intValue(),
                                list.get(3).intValue(), list.get(4).intValue());
                        holder.tvOrderTime.setText(formatted);
                    }
                } catch (Exception e) {
                    holder.tvOrderTime.setText("Lỗi ngày giờ");
                }
            }
        }

        if (invoice.get("table") != null) {
            Map<String, Object> table = (Map<String, Object>) invoice.get("table");
            String areaName = "Khu vực";
            if (table.get("area") != null) {
                Map<String, Object> area = (Map<String, Object>) table.get("area");
                areaName = (String) area.get("areaName");
            }
            holder.tvTableName.setText(areaName + " - " + table.get("tableName"));
        } else {
            holder.tvTableName.setText("Bàn (Không rõ)");
        }

        if (invoice.get("user") != null) {
            Map<String, Object> user = (Map<String, Object>) invoice.get("user");
            holder.tvStaffName.setText("NV: " + user.get("fullname"));
        } else {
            holder.tvStaffName.setText("NV: Ẩn danh");
        }

        if (invoice.get("totalAmount") != null) {
            Number total = (Number) invoice.get("totalAmount");
            holder.tvTotalAmount.setText(formatter.format(total.doubleValue()) + "đ");
        } else {
            holder.tvTotalAmount.setText("0đ");
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(invoice);
            }
        });
    }

    @Override
    public int getItemCount() {
        return invoices == null ? 0 : invoices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderTime, tvOrderId, tvTableName, tvStaffName, tvTotalAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderTime = itemView.findViewById(R.id.tvOrderTime);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvTableName = itemView.findViewById(R.id.tvTableName);
            tvStaffName = itemView.findViewById(R.id.tvStaffName);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
        }
    }
}
