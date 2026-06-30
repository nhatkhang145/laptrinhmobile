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

/**
 * Adapter hiển thị danh sách lịch sử ca làm việc.
 *
 * Mỗi item bao gồm:
 * - Ngày làm việc.
 * - Trạng thái ca (Đang mở/Đã đóng).
 * - Thời gian bắt đầu - kết thúc.
 * - Doanh thu của ca.
 * - Danh sách nhân viên tham gia.
 */
public class ShiftHistoryAdapter extends RecyclerView.Adapter<ShiftHistoryAdapter.ViewHolder> {

    private Context context;
    private List<Map<String, Object>> shiftList;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public ShiftHistoryAdapter(Context context, List<Map<String, Object>> shiftList) {
        this.context = context;
        this.shiftList = shiftList;
    }
    /**
     * Cập nhật danh sách ca làm việc mới
     * và làm mới RecyclerView.
     */
    public void setShifts(List<Map<String, Object>> shiftList) {
        this.shiftList = shiftList;
        notifyDataSetChanged();
    }
    /**
     * Tạo View cho mỗi item lịch sử ca.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lich_su_ca, parent, false);
        return new ViewHolder(view);
    }
    /**
     * Hiển thị dữ liệu của một ca làm việc lên giao diện.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Lấy thông tin ca tại vị trí hiện tại
        Map<String, Object> shift = shiftList.get(position);

        // Hiển thị ngày mở ca
        String dateStr = formatDate(shift.get("startTime"));
        holder.tvDate.setText(dateStr);

        // Hiển thị trạng thái của ca
        String status = (String) shift.get("status");
        if ("OPEN".equals(status)) {
            // Ca đang hoạt động
            holder.tvStatus.setText("ĐANG MỞ");
            holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // success green
        } else {
            // Ca đã kết thú
            holder.tvStatus.setText("ĐÃ ĐÓNG");
            holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#9E9E9E"))); // gray
        }

        // Hiển thị khoảng thời gian làm việc
        String startTime = parseTime(shift.get("startTime"));
        String endTime = parseTime(shift.get("endTime"));
        if (endTime.equals("N/A")) {
            // Ca chưa đóng
            holder.tvTimeRange.setText(startTime + " - Đang chạy");
        } else {
            // Ca đã đóng
            holder.tvTimeRange.setText(startTime + " - " + endTime);
        }

        // Hiển thị doanh thu của ca
        double revenue = 0;
        if (shift.get("totalRevenue") != null) {
            revenue = ((Number) shift.get("totalRevenue")).doubleValue();
        }
        holder.tvRevenue.setText(formatter.format(revenue) + "đ");

        // Hiển thị danh sách nhân viên tham gia ca
        String employees = (String) shift.get("employeeNames");
        if (employees == null || employees.isEmpty()) {
            employees = "Chưa có";
        }
        holder.tvEmployees.setText("Nhân sự: " + employees);
    }
    /**
     * Trả về số lượng ca làm việc cần hiển thị.
     */
    @Override
    public int getItemCount() {
        return shiftList != null ? shiftList.size() : 0;
    }
    /**
     * ViewHolder lưu tham chiếu đến các thành phần giao diện
     * của một item lịch sử ca.
     */
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
    /**
     * Chuyển dữ liệu thời gian sang định dạng ngày dd/MM/yyyy
     * để hiển thị trên giao diện.
     */
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
    /**
     * Chuyển dữ liệu thời gian sang định dạng HH:mm.
     * Dùng để hiển thị giờ bắt đầu và kết thúc ca.
     */
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
