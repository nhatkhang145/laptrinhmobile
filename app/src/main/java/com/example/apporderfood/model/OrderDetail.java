package com.example.apporderfood.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class OrderDetail {
    @SerializedName("id")           private Integer id;
    @SerializedName("menuItem")     private MenuItem menuItem;
    @SerializedName("quantity")     private Integer quantity;
    @SerializedName("priceAtSale")  private BigDecimal priceAtSale;
    @SerializedName("note")         private String note;
    @SerializedName("status")       private Integer status;
    // 0 = Nhap, 1 = Da gui (khoa NV), 2 = Da huy

    public Integer getId()            { return id; }
    public MenuItem getMenuItem()     { return menuItem; }
    public Integer getQuantity()      { return quantity; }
    public BigDecimal getPriceAtSale(){ return priceAtSale; }
    public String getNote()           { return note; }
    public Integer getStatus()        { return status; }

    /** Nhan vien co duoc phep sua/xoa khong? Chi duoc khi status = 0 (nhap) */
    public boolean isEditable()       { return status != null && status == 0; }

    /** Tinh tien cua dong nay */
    public BigDecimal getSubTotal() {
        if (priceAtSale == null || quantity == null) return BigDecimal.ZERO;
        return priceAtSale.multiply(BigDecimal.valueOf(quantity));
    }
}
