package com.example.apporderfood.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apporderfood.R;
import com.example.apporderfood.model.Area;
import java.util.List;

public class AreaFilterAdapter extends RecyclerView.Adapter<AreaFilterAdapter.ViewHolder> {

    private List<Area> areas;
    private int selectedPosition = 0;
    private OnAreaClickListener listener;

    public interface OnAreaClickListener {
        void onAreaClick(Area area);
    }

    public AreaFilterAdapter(List<Area> areas, OnAreaClickListener listener) {
        this.areas = areas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_tab, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Area area = areas.get(position);
        holder.tvAreaName.setText(area.getAreaName());

        if (selectedPosition == position) {
            holder.tvAreaName.setBackgroundResource(R.drawable.bg_tab_active_dark);
            holder.tvAreaName.setTextColor(holder.itemView.getContext().getColor(R.color.white));
        } else {
            holder.tvAreaName.setBackgroundResource(R.drawable.bg_tab_inactive);
            holder.tvAreaName.setTextColor(holder.itemView.getContext().getColor(R.color.text_secondary));
        }

        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onAreaClick(area);
            }
        });
    }

    @Override
    public int getItemCount() {
        return areas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAreaName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Reusing item_category_tab.xml which has tvCategoryName
            tvAreaName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}
