package com.example.apporderfood.model;

import com.google.gson.annotations.SerializedName;

/** Model User dung cho dang nhap va hien thi thong tin */
public class User {

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


    public User() {}

    public User(Integer id, String username, Integer role, Integer resId, String resName, String resDomain, String fullname) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.resId = resId;
        this.resName = resName;
        this.resDomain = resDomain;
        this.fullname = fullname;
    }

    // Getters and Setters
    @SerializedName("email")
    private String email;

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

    public String getEmail()    { return email; } // Bổ sung Getter

    /** Kiem tra co phai Quan ly khong */
    public boolean isManager()  { return role != null && role == 1; }
}
