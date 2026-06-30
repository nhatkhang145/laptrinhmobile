package com.zappy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho bảng shifts trong cơ sở dữ liệu.
 *
 * Mỗi bản ghi lưu thông tin của một ca làm việc, bao gồm:
 * - Nhà hàng sở hữu ca.
 * - Thời gian bắt đầu và kết thúc.
 * - Tiền đầu ca.
 * - Doanh thu của ca.
 * - Trạng thái ca (OPEN/CLOSED).
 * - Danh sách nhân viên tham gia ca.
 */
@Entity
@Table(name = "shifts")
public class Shift {
    /**
     * Khóa chính của ca làm việc.
     * Giá trị được tự động tăng.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Mã nhà hàng sở hữu ca làm việc.
     */

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;
    /**
     * Thời điểm mở ca.
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    /**
     * Thời điểm đóng ca.
     * Null nếu ca vẫn đang hoạt động.
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;
    /**
     * Số tiền đầu ca.
     */
    @Column(name = "starting_fund", nullable = false)
    private Double startingFund;
    /**
     * Tổng doanh thu của ca làm việc.
     */
    @Column(name = "total_revenue")
    private Double totalRevenue;
    /**
     * Trạng thái của ca.
     * OPEN   : Ca đang hoạt động.
     * CLOSED : Ca đã kết thúc.
     */
    @Column(name = "status", nullable = false)
    private String status; // "OPEN" or "CLOSED"
    /**
     * Danh sách tên nhân viên tham gia ca.
     * Các tên được lưu dưới dạng chuỗi, phân cách bởi dấu phẩy.
     */
    @Column(name = "employee_names", length = 500)
    private String employeeNames;
    /**
     * Danh sách ID nhân viên tham gia ca.
     * Ví dụ: "3,5,8"
     */
    @Column(name = "employee_ids", length = 500)
    private String employeeIds;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Double getStartingFund() {
        return startingFund;
    }

    public void setStartingFund(Double startingFund) {
        this.startingFund = startingFund;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEmployeeNames() {
        return employeeNames;
    }

    public void setEmployeeNames(String employeeNames) {
        this.employeeNames = employeeNames;
    }

    public String getEmployeeIds() {
        return employeeIds;
    }

    public void setEmployeeIds(String employeeIds) {
        this.employeeIds = employeeIds;
    }
}
