package com.example.apporderfood.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apporderfood.R;
import com.example.apporderfood.model.Area;
import java.util.List;

public class AreaManageAdapter extends RecyclerView.Adapter<AreaManageAdapter.AreaViewHolder> {

    private List<Area> AreaList;
    private OnAreaItemClickListener listener;

    public interface OnAreaItemClickListener {
        void onEditClick(Area item);
        void onDeleteClick(Area item);
        void onStatusToggleClick(Area item, int position);
        void onItemClick(Area item);
    }

    public AreaManageAdapter(List<Area> AreaList, OnAreaItemClickListener listener) {
        this.AreaList = AreaList;
        this.listener = listener;
    }

    public void removeItem(Area item) {
        int pos = AreaList.indexOf(item);
        if (pos >= 0) {
            AreaList.remove(pos);
            notifyItemRemoved(pos);
        }
    }

    /** Cập nhật status local và refresh item UI ngay lập tức */
    public void updateItemStatus(int position, boolean newStatus) {
        if (position < 0 || position >= AreaList.size()) return;
        AreaList.get(position).setIsActive(newStatus);
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public AreaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_area_manage, parent, false);
        return new AreaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AreaViewHolder holder, int position) {
        Area item = AreaList.get(position);
        holder.tvAreaName.setText(item.getAreaName());

        boolean active = item.getIsActive() != null ? item.getIsActive() : true;
        applyStatusStyle(holder.tvAreaStatus, active);

        holder.tvAreaInfo.setVisibility(View.GONE);

        // Nhấn badge trạng thái → toggle ngay
        holder.tvAreaStatus.setOnClickListener(v -> {
            if (listener != null) listener.onStatusToggleClick(item, holder.getAdapterPosition());
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });

        holder.ivAreaEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(item);
        });

        holder.ivAreaDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(item);
        });
    }

    private void applyStatusStyle(TextView tv, boolean active) {
        if (active) {
            tv.setText("● HOẠT ĐỘNG");
            tv.setBackgroundResource(R.drawable.bg_status_available);
            tv.setTextColor(tv.getContext().getColor(android.R.color.holo_green_dark));
        } else {
            tv.setText("○ TẠM ẨN");
            tv.setBackgroundResource(R.drawable.bg_status_paused);
            tv.setTextColor(tv.getContext().getColor(android.R.color.holo_orange_dark));
        }
    }

    @Override
    public int getItemCount() {
        return AreaList != null ? AreaList.size() : 0;
    }

    public static class AreaViewHolder extends RecyclerView.ViewHolder {
        TextView tvAreaName, tvAreaStatus, tvAreaInfo;
        ImageView ivAreaEdit, ivAreaDelete;

        public AreaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAreaName   = itemView.findViewById(R.id.tvAreaName);
            tvAreaStatus = itemView.findViewById(R.id.tvAreaStatus);
            tvAreaInfo   = itemView.findViewById(R.id.tvAreaInfo);
            ivAreaEdit   = itemView.findViewById(R.id.ivAreaEdit);
            ivAreaDelete = itemView.findViewById(R.id.ivAreaDelete);
        }
    }
}
