package com.example.apporderfood.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apporderfood.R;
import java.util.List;

/**
 * FoodManageAdapter
 * Adapter hỗ trợ hiển thị danh sách các món ăn lên RecyclerView.
 * Cung cấp khả năng gắn kết dữ liệu từ model (FoodItem) sang View (item_food_manage.xml).
 */
public class FoodManageAdapter extends RecyclerView.Adapter<FoodManageAdapter.FoodViewHolder> {

    private List<FoodItem> foodList;
    private OnFoodItemClickListener listener;

    /**
     * Interface định nghĩa sự kiện khi người dùng nhấn vào một món ăn trong danh sách.
     */
    public interface OnFoodItemClickListener {
        void onFoodItemClick(FoodItem item); // Gọi khi người dùng click vào một món ăn (thường để mở form sửa)
    }

    public FoodManageAdapter(List<FoodItem> foodList, OnFoodItemClickListener listener) {
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_manage, parent, false);
        return new FoodViewHolder(view);
    }

    /**
     * Gắn dữ liệu (Bind data) vào các thành phần giao diện của mỗi item.
     */
    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem item = foodList.get(position);
        
        // Cập nhật thông tin text: tên món, danh mục, giá và trạng thái
        holder.tvFoodName.setText(item.getName());
        holder.tvCategory.setText(item.getCategory() + " • " + item.getTag());
        holder.tvPrice.setText(item.getPrice());
        holder.tvStatus.setText(item.getStatus());

        // Cập nhật ảnh món ăn bằng thư viện Glide, nếu không có sẽ hiển thị ảnh mặc định
        if (item.getRawItem() != null && item.getRawItem().getImageUrl() != null) {
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                .load(item.getRawItem().getImageUrl())
                .placeholder(R.drawable.bg_food_image)
                .into(holder.ivFood);
        } else {
            holder.ivFood.setImageResource(R.drawable.bg_food_image);
        }

        // Thay đổi màu sắc của nhãn Trạng thái dựa vào chuỗi "CÒN MÓN" / "HẾT MÓN"
        if ("CÒN MÓN".equals(item.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_available);
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        } else if ("HẾT MÓN".equals(item.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_unavailable);
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_paused);
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
        }

        // Đăng ký sự kiện click cho toàn bộ khối item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFoodItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFood;
        TextView tvFoodName, tvCategory, tvPrice, tvStatus;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFood = itemView.findViewById(R.id.ivFood);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }

    /**
     * Lớp model trung gian (Data class) dùng để lưu trữ các trường dữ liệu 
     * đã được định dạng sẵn (chuỗi) để hiển thị lên UI, kèm theo object MenuItem gốc (rawItem)
     * dùng khi cần truy xuất ID hay các dữ liệu khác.
     */
    public static class FoodItem {
        private String name;
        private String category;
        private String tag;
        private String price;
        private String status;
        private com.example.apporderfood.model.MenuItem rawItem; // To hold the actual object

        public FoodItem(String name, String category, String tag, String price, String status, com.example.apporderfood.model.MenuItem rawItem) {
            this.name = name;
            this.category = category;
            this.tag = tag;
            this.price = price;
            this.status = status;
            this.rawItem = rawItem;
        }

        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getTag() { return tag; }
        public String getPrice() { return price; }
        public String getStatus() { return status; }
        public com.example.apporderfood.model.MenuItem getRawItem() { return rawItem; }
    }

}
