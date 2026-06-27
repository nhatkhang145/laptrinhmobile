package com.example.apporderfood.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.R;
import com.example.apporderfood.model.OrderDetail;

import java.text.DecimalFormat;
import java.util.List;

public class InvoiceItemAdapter extends RecyclerView.Adapter<InvoiceItemAdapter.ViewHolder> {

    private Context context;
    private List<OrderDetail> items;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public InvoiceItemAdapter(Context context, List<OrderDetail> items) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_invoice_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDetail detail = items.get(position);

        String itemName = detail.getMenuItem() != null ? detail.getMenuItem().getItemName() : "Món (Không xác định)";
        holder.tvName.setText(itemName);
        
        holder.tvQty.setText(String.valueOf(detail.getQuantity()));
        
        if (detail.getPriceAtSale() != null) {
            holder.tvPrice.setText(formatter.format(detail.getPriceAtSale()));
        } else {
            holder.tvPrice.setText("0");
        }
        
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
        TextView tvName, tvQty, tvPrice, tvSubTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvInvoiceItemName);
            tvQty = itemView.findViewById(R.id.tvInvoiceItemQty);
            tvPrice = itemView.findViewById(R.id.tvInvoiceItemPrice);
            tvSubTotal = itemView.findViewById(R.id.tvInvoiceItemSubTotal);
        }
    }
}
