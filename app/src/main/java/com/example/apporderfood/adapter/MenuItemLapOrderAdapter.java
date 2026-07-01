package com.example.apporderfood.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.apporderfood.R;
import com.example.apporderfood.model.CartItem;
import com.example.apporderfood.model.MenuItem;
import com.google.android.material.imageview.ShapeableImageView;
import com.mikepenz.iconics.view.IconicsImageView;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuItemLapOrderAdapter extends RecyclerView.Adapter<MenuItemLapOrderAdapter.ViewHolder> {

    private Context context;
    private List<MenuItem> menuItems;
    // Giỏ hàng tạm thời, key là ID món ăn, value là thông tin số lượng và ghi chú
    private Map<Integer, CartItem> cartMap = new HashMap<>();
    private OnCartChangeListener listener;

    // Interface để gửi sự kiện mỗi khi giỏ hàng có sự thay đổi (thêm/bớt món)
    public interface OnCartChangeListener {
        void onCartUpdated(Map<Integer, CartItem> updatedCart);
    }

    public MenuItemLapOrderAdapter(Context context, List<MenuItem> menuItems, Map<Integer, CartItem> cartMap, OnCartChangeListener listener) {
        this.context = context;
        this.menuItems = menuItems;
        this.cartMap = cartMap != null ? cartMap : new HashMap<>(); //B10 Đc truyền từ LapOrderActivity
        this.listener = listener;
    }

    public void setMenuItems(List<MenuItem> items) {
        this.menuItems = items;
        notifyDataSetChanged();
    }

    /**
     * CẬP NHẬT LẠI GIỎ HÀNG TẠM TỪ ACTIVITY
     */
    public void setCartMap(Map<Integer, CartItem> cartMap) {
        this.cartMap = cartMap != null ? cartMap : new HashMap<>();
        notifyDataSetChanged();
    }

    /**
     * TẠO GIAO DIỆN (NẠP XML) CHO TỪNG DÒNG MÓN ĂN
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_menu_lap_order, parent, false);
        return new ViewHolder(view);
    }

    /**
     * ĐỔ DỮ LIỆU VÀO GIAO DIỆN CỦA TỪNG MÓN ĂN
     * Bao gồm: hiển thị tên, giá, ảnh, trạng thái giỏ hàng và gắn sự kiện click.
     */

    //B11 khi ng dùng bấm nút thêm hàm này sẽ bắt sự kện và cập nhật số lượng
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuItem item = menuItems.get(position);

        holder.tvFoodName.setText(item.getItemName() != null ? item.getItemName() : "");
        
        if (item.getUnit() != null) {
            holder.tvFoodDesc.setText("ĐVT: " + item.getUnit().getUnitName());
        } else {
            holder.tvFoodDesc.setText("");
        }

        if (item.getPrice() != null) {
            DecimalFormat formatter = new DecimalFormat("#,###");
            holder.tvFoodPrice.setText(formatter.format(item.getPrice()));
        } else {
            holder.tvFoodPrice.setText("0");
        }

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.bg_food_image)
                    .into(holder.ivFoodImage);
        } else {
            holder.ivFoodImage.setImageResource(0);
        }

        // Kiểm tra xem món ăn này có đang nằm trong giỏ hàng tạm (cartMap) hay không
        CartItem cartItem = cartMap.get(item.getId());
        int qty = cartItem != null ? cartItem.getQuantity() : 0;

        // Nếu số lượng > 0 (đã chọn), ẩn nút "Thêm", hiện cụm nút [+ 1 -]
        if (qty > 0) {
            holder.btnAddItem.setVisibility(View.GONE);
            holder.llQuantityControl.setVisibility(View.VISIBLE);
            holder.tvQuantity.setText(String.valueOf(qty));
        } else {
            // Nếu chưa chọn, chỉ hiện nút "Thêm"
            holder.btnAddItem.setVisibility(View.VISIBLE);
            holder.llQuantityControl.setVisibility(View.GONE);
        }

        // Xử lý sự kiện bấm nút Thêm/Tăng/Giảm số lượng món
        holder.btnAddItem.setOnClickListener(v -> updateQuantity(item, 1));
        holder.btnPlus.setOnClickListener(v -> updateQuantity(item, qty + 1));
        holder.btnMinus.setOnClickListener(v -> updateQuantity(item, qty - 1));

        // Xử lý sự kiện bấm nút Thêm ghi chú cho món ăn
        holder.btnNote.setOnClickListener(v -> {
            CartItem current = cartMap.get(item.getId());
            String existingNote = current != null && current.getNote() != null ? current.getNote() : "";

            final android.app.Dialog dialog = new android.app.Dialog(context);
            dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_edit_note);

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            }

            TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
            tvTitle.setText("Ghi chú cho " + item.getItemName());

            final EditText input = dialog.findViewById(R.id.etNote);
            input.setText(existingNote);

            TextView btnSave = dialog.findViewById(R.id.btnSave);
            TextView btnCancel = dialog.findViewById(R.id.btnCancel);

            btnSave.setOnClickListener(v1 -> {
                String newNote = input.getText().toString();
                if (current != null) {
                    current.setNote(newNote);
                    if (listener != null) listener.onCartUpdated(cartMap);
                }
                dialog.dismiss();
            });

            btnCancel.setOnClickListener(v12 -> dialog.dismiss());

            dialog.show();
        });
    }

    /**
     * Hàm cập nhật số lượng món ăn trong giỏ hàng tạm
     * @param item món ăn cần cập nhật
     * @param newQty số lượng mới
     */

    //B12 Luồng xử lý lõi bên LapOrderActivity
    private void updateQuantity(MenuItem item, int newQty) {
        if (newQty > 0) {
            CartItem cartItem = cartMap.get(item.getId());
            if (cartItem == null) {
                // Nếu món chưa có trong giỏ, tạo mới và đưa vào cartMap
                cartItem = new CartItem(item, newQty, "");
                cartMap.put(item.getId(), cartItem);
            } else {
                // Nếu đã có, chỉ việc cập nhật số lượng
                cartItem.setQuantity(newQty);
            }
        } else {
            // Nếu số lượng giảm về 0, xóa món khỏi giỏ hàng
            cartMap.remove(item.getId());
        }
        
        // Cập nhật lại giao diện ngay lập tức
        //B13 Gọi hàm này để cập nhật giao diện hiển thị số lượng đổi nút thêm thành nút tăng giảm
        notifyDataSetChanged();
        
        // B14 Gửi thông báo ra Activity (LapOrderActivity) để biết giỏ hàng đã thay đổi
        if (listener != null) {
            listener.onCartUpdated(cartMap);
        }
    }

    /**
     * TRẢ VỀ TỔNG SỐ LƯỢNG MÓN ĂN CÓ TRONG DANH SÁCH
     */
    @Override
    public int getItemCount() {
        return menuItems == null ? 0 : menuItems.size();
    }

    /**
     * LỚP VIEWHOLDER: CHỨA CÁC THÀNH PHẦN GIAO DIỆN CỦA 1 MÓN ĂN
     * Ánh xạ (findViewById) các thành phần giao diện để onBindViewHolder sử dụng.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivFoodImage;
        TextView tvFoodName, tvFoodDesc, tvFoodPrice, tvQuantity;
        IconicsImageView btnAddItem, btnPlus, btnMinus, btnNote;
        LinearLayout llQuantityControl;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodDesc = itemView.findViewById(R.id.tvFoodDesc);
            tvFoodPrice = itemView.findViewById(R.id.tvFoodPrice);
            btnAddItem = itemView.findViewById(R.id.btnAddItem);
            
            llQuantityControl = itemView.findViewById(R.id.llQuantityControl);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnNote = itemView.findViewById(R.id.btnNote);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }
    }
}
