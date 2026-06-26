package com.example.apporderfood.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ShiftHistoryAdapter extends RecyclerView.Adapter<ShiftHistoryAdapter.ViewHolder> {

    private Context context;
    private List<Map<String, Object>> shiftList;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public ShiftHistoryAdapter(Context context, List<Map<String, Object>> shiftList) {
        this.context = context;
        this.shiftList = shiftList;
    }

    public void setShifts(List<Map<String, Object>> shiftList) {
        this.shiftList = shiftList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lich_su_ca, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> shift = shiftList.get(position);

        // Date
        String dateStr = formatDate(shift.get("startTime"));
        holder.tvDate.setText(dateStr);

        // Status
        String status = (String) shift.get("status");
        if ("OPEN".equals(status)) {
            holder.tvStatus.setText("ĐANG MỞ");
            holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // success green
        } else {
            holder.tvStatus.setText("ĐÃ ĐÓNG");
            holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#9E9E9E"))); // gray
        }

        // Time Range
        String startTime = parseTime(shift.get("startTime"));
        String endTime = parseTime(shift.get("endTime"));
        if (endTime.equals("N/A")) {
            holder.tvTimeRange.setText(startTime + " - Đang chạy");
        } else {
            holder.tvTimeRange.setText(startTime + " - " + endTime);
        }

        // Revenue
        double revenue = 0;
        if (shift.get("totalRevenue") != null) {
            revenue = ((Number) shift.get("totalRevenue")).doubleValue();
        }
        holder.tvRevenue.setText(formatter.format(revenue) + "đ");

        // Employees
        String employees = (String) shift.get("employeeNames");
        if (employees == null || employees.isEmpty()) {
            employees = "Chưa có";
        }
        holder.tvEmployees.setText("Nhân sự: " + employees);
    }

    @Override
    public int getItemCount() {
        return shiftList != null ? shiftList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvStatus, tvTimeRange, tvRevenue, tvEmployees;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTimeRange = itemView.findViewById(R.id.tvTimeRange);
            tvRevenue = itemView.findViewById(R.id.tvRevenue);
            tvEmployees = itemView.findViewById(R.id.tvEmployees);
        }
    }

    private String formatDate(Object dateObj) {
        if (dateObj == null) return "N/A";
        if (dateObj instanceof String) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                java.util.Date date = parser.parse((String) dateObj);
                SimpleDateFormat printer = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return printer.format(date);
            } catch (Exception e) {
                return (String) dateObj;
            }
        } else if (dateObj instanceof List) {
            List<Number> list = (List<Number>) dateObj;
            if (list.size() >= 3) {
                return String.format(Locale.getDefault(), "%02d/%02d/%04d",
                        list.get(2).intValue(), list.get(1).intValue(), list.get(0).intValue());
            }
        }
        return "N/A";
    }

    private String parseTime(Object dateObj) {
        if (dateObj == null) return "N/A";
        if (dateObj instanceof String) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                java.util.Date date = parser.parse((String) dateObj);
                SimpleDateFormat printer = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return printer.format(date);
            } catch (Exception e) {
                return "";
            }
        } else if (dateObj instanceof List) {
            List<Number> list = (List<Number>) dateObj;
            if (list.size() >= 5) {
                return String.format(Locale.getDefault(), "%02d:%02d",
                        list.get(3).intValue(), list.get(4).intValue());
            }
        }
        return "N/A";
    }
}
