package com.example.apporderfood.adapter;

import android.content.Context;
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

import java.util.List;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.TableViewHolder> {

    private Context context;
    private List<TableModel> tableList;
    private OnTableClickListener listener;

    public interface OnTableClickListener {
        void onTableClick(TableModel table);
    }

    public TableAdapter(Context context, List<TableModel> tableList, OnTableClickListener listener) {
        this.context = context;
        this.tableList = tableList;
        this.listener = listener;
    }

    public void setTables(List<TableModel> tableList) {
        this.tableList = tableList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_table_card, parent, false);
        return new TableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        TableModel table = tableList.get(position);
        
        holder.tvTableName.setText(table.getTableName());
        
        // Extract number from name if possible (e.g. "Bàn 01" -> "01")
        String number = table.getTableName().replaceAll("[^0-9]", "");
        if (number.isEmpty()) {
            number = String.valueOf(position + 1);
        } else if (number.length() == 1) {
            number = "0" + number;
        } else if (number.length() > 2) {
            number = number.substring(number.length() - 2);
        }
        holder.tvTableNumberIcon.setText(number);

        if ("ĐANG KHÓA".equals(table.getStatus()) || "BẢO TRÌ".equals(table.getStatus())) {
            holder.cardBan.setBackgroundResource(R.drawable.bg_table_empty);
            holder.tvTableNumberIcon.setBackgroundResource(R.drawable.bg_table_number_empty);
            holder.tvTableNumberIcon.setTextColor(Color.parseColor("#9E9E9E")); // Gray out
            holder.tvTableName.setTextColor(Color.parseColor("#9E9E9E"));
            holder.tvTableStatus.setText("Đang khóa");
            holder.tvTableStatus.setTextColor(Color.parseColor("#EF4444")); // Red
        } else if (table.isOccupied()) {
            holder.cardBan.setBackgroundResource(R.drawable.bg_table_occupied);
            holder.tvTableNumberIcon.setBackgroundResource(R.drawable.bg_table_number_occupied);
            holder.tvTableNumberIcon.setTextColor(Color.parseColor("#B0C4DE"));
            holder.tvTableName.setTextColor(context.getResources().getColor(R.color.surface));
            holder.tvTableStatus.setText("Có khách");
            holder.tvTableStatus.setTextColor(Color.parseColor("#B0C4DE"));
        } else {
            holder.cardBan.setBackgroundResource(R.drawable.bg_table_empty);
            holder.tvTableNumberIcon.setBackgroundResource(R.drawable.bg_table_number_empty);
            holder.tvTableNumberIcon.setTextColor(context.getResources().getColor(R.color.text_primary));
            holder.tvTableName.setTextColor(context.getResources().getColor(R.color.text_primary));
            holder.tvTableStatus.setText("Trống");
            holder.tvTableStatus.setTextColor(context.getResources().getColor(R.color.text_secondary));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTableClick(table);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tableList != null ? tableList.size() : 0;
    }

    public static class TableViewHolder extends RecyclerView.ViewHolder {
        LinearLayout cardBan;
        TextView tvTableNumberIcon, tvTableName, tvTableStatus;

        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            cardBan = itemView.findViewById(R.id.cardBan);
            tvTableNumberIcon = itemView.findViewById(R.id.tvTableNumberIcon);
            tvTableName = itemView.findViewById(R.id.tvTableName);
            tvTableStatus = itemView.findViewById(R.id.tvTableStatus);
        }
    }
}
