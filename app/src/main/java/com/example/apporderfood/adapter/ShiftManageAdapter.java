package com.example.apporderfood.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.R;
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShiftManageAdapter extends RecyclerView.Adapter<ShiftManageAdapter.ViewHolder> {

    private Context context;
    private List<Map<String, String>> shifts;

    public ShiftManageAdapter(Context context) {
        this.context = context;
        this.shifts = new ArrayList<>();
    }

    public void setShifts(List<Map<String, String>> shifts) {
        this.shifts = shifts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shift, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, String> shift = shifts.get(position);

        holder.tvShiftName.setText(shift.get("name"));
        holder.tvShiftTime.setText(shift.get("time"));

        holder.btnEdit.setOnClickListener(v -> {
            // TODO: Edit shift
        });

        holder.btnDelete.setOnClickListener(v -> {
            // TODO: Delete shift
        });
    }

    @Override
    public int getItemCount() {
        return shifts == null ? 0 : shifts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvShiftName, tvShiftTime;
        IconicsImageView btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShiftName = itemView.findViewById(R.id.tvShiftName);
            tvShiftTime = itemView.findViewById(R.id.tvShiftTime);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
