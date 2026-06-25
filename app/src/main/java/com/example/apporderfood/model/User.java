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

    @SerializedName("email")
    private String email;

    // Getters
    public Integer getId()      { return id; }
    public String getUsername() { return username; }
    public Integer getRole()    { return role; }
    public Integer getResId()   { return resId; }
    public String getResName()  { return resName; }
    public String getResDomain(){ return resDomain; }
    public String getFullname() { return fullname; }

    public String getEmail()    { return email; } // Bổ sung Getter

    /** Kiem tra co phai Quan ly khong */
    public boolean isManager()  { return role != null && role == 1; }
}
