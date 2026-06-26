package com.zappy.model;

public class OTPData {

    private final String otp;
    private final long expireTime;

    public OTPData(String otp, long expireTime) {
        this.otp = otp;
        this.expireTime = expireTime;
    }

    public String getOtp() {
        return otp;
    }

    public long getExpireTime() {
        return expireTime;
    }
}