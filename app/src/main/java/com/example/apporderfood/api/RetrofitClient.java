package com.example.apporderfood.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * RetrofitClient - Singleton ket noi den Spring Boot backend
 *
 * Khi chay tren May ao (Emulator): dung 10.0.2.2
 * Khi chay tren Dien thoai that:  dung IP may tinh cua ban
 *   Vi du: http://192.168.1.100:8080/
 *   (Mo CMD -> ipconfig -> tim IPv4 Address)
 */
public class RetrofitClient {

    // ⚠️ THAY IP NÀY = IP máy tính của bạn khi test trên điện thoại thật
    // Khi dùng máy ảo Android Studio thì giữ nguyên 10.0.2.2
    private static final String BASE_URL = "http://10.0.2.2:8080/";

    private static Retrofit instance = null;

    public static Retrofit getInstance() {
        if (instance == null) {
            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // Gson tu dong chuyen JSON
                    .build();
        }
        return instance;
    }

    /** Lay API service de goi cac endpoint */
    public static ZappyApiService getApiService() {
        return getInstance().create(ZappyApiService.class);
    }

    /** Doi BASE_URL (dung khi switch giua emulator va dien thoai that) */
    public static void setBaseUrl(String url) {
        instance = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
