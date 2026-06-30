package com.example.apporderfood.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporderfood.R;
import com.example.apporderfood.model.TableModel;

import java.util.List;

/**
 * ============================================================
 *  TABLE IN AREA ADAPTER - HIỂN THỊ DANH SÁCH BÀN TRONG KHU VỰC
 * ============================================================
 *
 * Adapter này dùng cho RecyclerView hiển thị danh sách bàn bên trong
 * 1 khu vực cụ thể (ví dụ: màn hình "Chi tiết khu vực").
 *
 * Mỗi item hiển thị:
 *   - Tên bàn (ví dụ: "Bàn 01")
 *   - Số ghế (ví dụ: "Số ghế: 4")
 *   - Trạng thái bàn với màu sắc trực quan:
 *       🟠 "ĐANG KHÓA"    → nền cam (bàn tạm dừng hoạt động)
 *       🔴 "ĐANG CÓ KHÁCH" → nền đỏ (bàn đang phục vụ)
 *       🟢 "ĐANG TRỐNG"    → nền xanh (bàn sẵn sàng)
 *
 * Kế thừa RecyclerView.Adapter<ViewHolder> - chuẩn của Android RecyclerView.
 */
public class TableInAreaAdapter extends RecyclerView.Adapter<TableInAreaAdapter.ViewHolder> {

    // Danh sách bàn cần hiển thị
    private List<TableModel> items;

    /**
     * Constructor: Nhận danh sách bàn ban đầu khi tạo adapter.
     *
     * @param items Danh sách TableModel từ API
     */
    public TableInAreaAdapter(List<TableModel> items) {
        this.items = items;
    }

    /**
     * Cập nhật toàn bộ danh sách và làm mới RecyclerView.
     *
     * Gọi khi dữ liệu thay đổi (ví dụ: sau khi gọi API lại).
     * notifyDataSetChanged() báo cho RecyclerView vẽ lại toàn bộ danh sách.
     *
     * @param items Danh sách mới
     */
    public void setItems(List<TableModel> items) {
        this.items = items;
        notifyDataSetChanged(); // Báo RecyclerView vẽ lại tất cả item
    }

    // ============================================================
    //  3 PHƯƠNG THỨC BẮT BUỘC CỦA RecyclerView.Adapter
    // ============================================================

    /**
     * [Bước 1] TẠO VIEWHOLDER - Inflate (phình to/tạo ra) layout XML thành View.
     *
     * Được gọi khi RecyclerView cần thêm 1 item mới (chỉ gọi khi không có View tái sử dụng).
     * LayoutInflater đọc file XML (item_table_in_area.xml) và tạo thành đối tượng View.
     *
     * @param parent   ViewGroup chứa RecyclerView
     * @param viewType Loại view (dùng khi có nhiều loại item khác nhau, ở đây = 0)
     * @return ViewHolder chứa các View của 1 item
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout XML: item_table_in_area.xml → View object
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_table_in_area, parent, false);
        return new ViewHolder(v); // Bọc View vào ViewHolder để tái sử dụng
    }

    /**
     * [Bước 2] GÁN DỮ LIỆU VÀO VIEWHOLDER - Bind data cho từng item.
     *
     * Được gọi mỗi khi RecyclerView hiển thị 1 item (khi scroll vào vùng nhìn thấy).
     * Lấy TableModel ở vị trí position và hiển thị thông tin lên các TextView.
     *
     * Logic hiển thị trạng thái (ưu tiên theo thứ tự):
     *   1. Kiểm tra status == "ĐANG KHÓA" trước (bàn bị khóa có thể vừa trống vừa khóa)
     *   2. Kiểm tra isOccupied (bàn có khách)
     *   3. Mặc định: bàn trống
     *
     * @param holder   ViewHolder cần gán dữ liệu vào
     * @param position Vị trí của item trong danh sách (0-based)
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Lấy dữ liệu bàn tại vị trí position
        TableModel item = items.get(position);

        // Hiển thị tên bàn
        holder.tvTableName.setText(item.getTableName());

        // Hiển thị số ghế (nếu null thì mặc định = 0)
        int seats = item.getSeats() != null ? item.getSeats() : 0;
        holder.tvTableSeats.setText("Số ghế: " + seats);

        // ---- Logic hiển thị trạng thái bàn với màu sắc ----
        String status     = item.getStatus();       // "HOẠT ĐỘNG" hoặc "ĐANG KHÓA"
        boolean locked    = "ĐANG KHÓA".equals(status); // Bàn bị khóa (bảo trì, tạm dừng)
        boolean isOccupied = item.isOccupied();     // Bàn đang có khách

        if (locked) {
            // Ưu tiên 1: Bàn đang khóa → màu cam, background đặc biệt
            holder.tvTableStatus.setText("ĐANG KHÓA");
            holder.tvTableStatus.setBackgroundResource(R.drawable.bg_status_paused);
            holder.tvTableStatus.setTextColor(
                    holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));

        } else if (isOccupied) {
            // Ưu tiên 2: Bàn đang có khách → màu đỏ
            holder.tvTableStatus.setText("ĐANG CÓ KHÁCH");
            holder.tvTableStatus.setBackgroundResource(R.drawable.bg_status_unavailable);
            holder.tvTableStatus.setTextColor(
                    holder.itemView.getContext().getColor(R.color.white));

        } else {
            // Mặc định: Bàn trống → màu xanh
            holder.tvTableStatus.setText("ĐANG TRỐNG");
            holder.tvTableStatus.setBackgroundResource(R.drawable.bg_status_available);
            holder.tvTableStatus.setTextColor(
                    holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        }
    }

    /**
     * [Bước 3] TRẢ VỀ SỐ LƯỢNG ITEM - RecyclerView cần biết có bao nhiêu item để hiển thị.
     *
     * Trả về 0 nếu danh sách null (tránh NullPointerException).
     *
     * @return Số lượng bàn trong danh sách
     */
    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    // ============================================================
    //  VIEWHOLDER - LƯU THAM CHIẾU ĐẾN CÁC VIEW TRONG 1 ITEM
    // ============================================================

    /**
     * ViewHolder chứa tham chiếu đến các View trong layout item_table_in_area.xml.
     *
     * Mục đích: Tránh gọi findViewById() lặp đi lặp lại mỗi lần bind data
     * (tốn kém vì phải duyệt cây View) → chỉ gọi 1 lần khi tạo ViewHolder.
     *
     * Các View trong 1 item bàn:
     *   tvTableName:   Tên bàn
     *   tvTableStatus: Trạng thái bàn (với background màu)
     *   tvTableSeats:  Số ghế
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTableName;    // Ví dụ: "Bàn 01"
        TextView tvTableStatus;  // Ví dụ: "ĐANG TRỐNG" (màu xanh)
        TextView tvTableSeats;   // Ví dụ: "Số ghế: 4"

        /**
         * Constructor: Ánh xạ các View từ layout item vào biến.
         * Được gọi 1 lần duy nhất khi tạo ViewHolder (trong onCreateViewHolder).
         *
         * @param itemView View gốc của 1 item trong RecyclerView
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTableName   = itemView.findViewById(R.id.tvTableName);
            tvTableStatus = itemView.findViewById(R.id.tvTableStatus);
            tvTableSeats  = itemView.findViewById(R.id.tvTableSeats);
        }
    }
}
