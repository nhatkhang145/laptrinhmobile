package com.example.apporderfood.model;
import java.io.Serializable;
import com.google.gson.annotations.SerializedName;

/** Model User dung cho dang nhap va hien thi thong tin */
public class User implements Serializable {

    @SerializedName("id")
    private Integer id;

    @SerializedName("username")
    private String username;

    @SerializedName("role")
    private Integer role;   // 1 = Quan ly, 0 = Nhan vien

    @SerializedName("resId")
    private Integer resId;

    @SerializedName("resName")
    private String resName;

    @SerializedName("resDomain")
    private String resDomain;

    @SerializedName("fullname")
    private String fullname;

    @SerializedName("email")
    private String email;

    @SerializedName("isOnline")
    private Boolean isOnline;

    @SerializedName("isActive")
    private Boolean isActive;

    public User() {}

    public User(Integer id, String username, Integer role, Integer resId, String resName, String resDomain, String fullname,String email) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.resId = resId;
        this.resName = resName;
        this.resDomain = resDomain;
        this.fullname = fullname;
        this.email = email;
    }

    // Getters and Setters


    // Getters
    public Integer getId()      { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getRole()    { return role; }
    public void setRole(Integer role) { this.role = role; }

    public Integer getResId()   { return resId; }
    public void setResId(Integer resId) { this.resId = resId; }

    public String getResName()  { return resName; }
    public void setResName(String resName) { this.resName = resName; }

    public String getResDomain(){ return resDomain; }
    public void setResDomain(String resDomain) { this.resDomain = resDomain; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getEmail()    { return email; }
    public void setEmail(String email) { this.email = email; }

    public Boolean getIsOnline() { return isOnline; }
    public void setIsOnline(Boolean isOnline) { this.isOnline = isOnline; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    /** Kiem tra co phai Quan ly khong */
    public boolean isManager()  { return role != null && role == 1; }
}
