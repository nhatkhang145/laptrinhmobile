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
import java.util.List;
import java.util.Map;

public class CancelManageAdapter extends RecyclerView.Adapter<CancelManageAdapter.ViewHolder> {
    private List<Map<String, Object>> cancelList;
    private Context context;
    private DecimalFormat formatter = new DecimalFormat("#,### đ");

    public CancelManageAdapter(Context context, List<Map<String, Object>> cancelList) {
        this.context = context;
        this.cancelList = cancelList;
    }

    public void updateData(List<Map<String, Object>> newData) {
        this.cancelList = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cancel_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> item = cancelList.get(position);

        String itemName = "Không rõ món";
        if (item.containsKey("itemName") && item.get("itemName") != null) {
            itemName = String.valueOf(item.get("itemName"));
        } else if (item.containsKey("menuItem")) {
            Object menuItemObj = item.get("menuItem");
            if (menuItemObj instanceof Map) {
                Map<String, Object> menuMap = (Map<String, Object>) menuItemObj;
                if (menuMap.containsKey("itemName") && menuMap.get("itemName") != null) {
                    itemName = String.valueOf(menuMap.get("itemName"));
                }
            }
        }
        holder.tvItemName.setText(itemName);

        String time = "";
        if (item.containsKey("updatedAt") && item.get("updatedAt") != null) {
            time = String.valueOf(item.get("updatedAt"));
            // Format time if needed, backend usually returns ISO string
            if (time.length() > 16) {
                time = time.replace("T", " ").substring(0, 16);
            }
        }
        holder.tvCancelTime.setText(time);

        double price = 0;
        if (item.containsKey("price") && item.get("price") != null) {
            try {
                price = Double.parseDouble(String.valueOf(item.get("price")));
            } catch (Exception e) {}
        }
        
        int quantity = 1;
        if (item.containsKey("quantity") && item.get("quantity") != null) {
            try {
                quantity = (int) Double.parseDouble(String.valueOf(item.get("quantity")));
            } catch (Exception e) {}
        }
        
        double total = price * quantity;
        holder.tvPrice.setText(formatter.format(total));

        String reason = "Không có lý do";
        if (item.containsKey("cancelReason") && item.get("cancelReason") != null) {
            reason = String.valueOf(item.get("cancelReason"));
            if (reason.trim().isEmpty()) reason = "Không có lý do";
        }
        holder.tvCancelReason.setText(reason);
    }

    @Override
    public int getItemCount() {
        return cancelList != null ? cancelList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvCancelTime, tvPrice, tvCancelReason;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvCancelTime = itemView.findViewById(R.id.tvCancelTime);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvCancelReason = itemView.findViewById(R.id.tvCancelReason);
        }
    }
}
