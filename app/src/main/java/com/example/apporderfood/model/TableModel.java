package com.example.apporderfood.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/** Model Ban an - is_occupied dieu khien mau hien thi do/xanh */
public class TableModel implements Serializable {

    @SerializedName("id")
    private Integer id;

    @SerializedName("tableName")
    private String tableName;

    @SerializedName("isOccupied")
    private Boolean isOccupied;  // true = do (co khach), false = xanh (trong)

    @SerializedName("area")
    private Area area;

    @SerializedName("activeUserId")
    private Integer activeUserId;

    @SerializedName("status")
    private String status; // HOẠT ĐỘNG, ĐANG KHÓA, BẢO TRÌ

    @SerializedName("seats")
    private Integer seats;

    public TableModel() {}

    public TableModel(Integer id, String tableName, Boolean isOccupied, Area area) {
        this.id = id;
        this.tableName = tableName;
        this.isOccupied = isOccupied;
        this.area = area;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public Boolean isOccupied() { return isOccupied != null && isOccupied; }
    public void setOccupied(Boolean occupied) { isOccupied = occupied; }

    public Area getArea() { return area; }
    public void setArea(Area area) { this.area = area; }

    public Integer getActiveUserId() { return activeUserId; }
    public void setActiveUserId(Integer activeUserId) { this.activeUserId = activeUserId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getSeats() { return seats; }
    public void setSeats(Integer seats) { this.seats = seats; }
}
