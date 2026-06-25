package com.example.apporderfood.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.math.BigDecimal;

public class OrderDetail implements Serializable {
    @SerializedName("id")
    private Integer id;

    @SerializedName("menuItem")
    private MenuItem menuItem;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("priceAtSale")
    private BigDecimal priceAtSale;

    @SerializedName("note")
    private String note;

    @SerializedName("status")
    private Integer status; // 0 = Nhap, 1 = Da gui (khoa NV), 2 = Da huy

    public OrderDetail() {}

    public OrderDetail(Integer id, MenuItem menuItem, Integer quantity, BigDecimal priceAtSale, String note, Integer status) {
        this.id = id;
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.priceAtSale = priceAtSale;
        this.note = note;
        this.status = status;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public MenuItem getMenuItem() { return menuItem; }
    public void setMenuItem(MenuItem menuItem) { this.menuItem = menuItem; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getPriceAtSale() { return priceAtSale; }
    public void setPriceAtSale(BigDecimal priceAtSale) { this.priceAtSale = priceAtSale; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    /** Nhan vien co duoc phep sua/xoa khong? Chi duoc khi status = 0 (nhap) */
    public boolean isEditable() { return status != null && status == 0; }

    /** Tinh tien cua dong nay */
    public BigDecimal getSubTotal() {
        if (priceAtSale == null || quantity == null) return BigDecimal.ZERO;
        return priceAtSale.multiply(BigDecimal.valueOf(quantity));
    }
}
