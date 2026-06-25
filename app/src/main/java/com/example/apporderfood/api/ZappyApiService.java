package com.example.apporderfood.api;

import com.example.apporderfood.model.Area;
import com.example.apporderfood.model.Category;
import com.example.apporderfood.model.MenuItem;
import com.example.apporderfood.model.OrderDetail;
import com.example.apporderfood.model.Restaurant;
import com.example.apporderfood.model.TableModel;
import com.example.apporderfood.model.Unit;
import com.example.apporderfood.model.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * ZappyApiService - Dinh nghia tat ca API endpoint cua Zappy Backend
 * Retrofit tu dong tao implementation, chi can goi ham la xong!
 */
public interface ZappyApiService {

    // ==========================================
    // RESTAURANTS
    // ==========================================
    @GET("api/restaurants/domain/{domain}")
    Call<Restaurant> getRestaurantByDomain(@Path("domain") String domain);

    @POST("api/restaurants")
    Call<Restaurant> createRestaurant(@Body Map<String, String> data);

    // ==========================================
    // DANG NHAP
    // POST body: { "domain": "mycayseoulthuduc", "username": "nv01", "password":
    // "123" }
    // Tra ve: { "id", "username", "role", "resId", "resName", "resDomain" }
    // ==========================================
    @POST("api/users/login")
    Call<User> login(@Body Map<String, String> loginData);

    @GET("api/users/restaurant/{resId}")
    Call<List<User>> getUsersByRestaurant(@Path("resId") Integer resId);

    @POST("api/users")
    Call<User> createUser(@Body Map<String, Object> data);

    @PUT("api/users/{id}/password")
    Call<Map> changePassword(@Path("id") Integer id, @Body Map<String, String> data);

    @DELETE("api/users/{id}")
    Call<Map> deleteUser(@Path("id") Integer id);

    // ==========================================
    // AREAS (Khu vuc phuc vu)
    // ==========================================
    @GET("api/areas/restaurant/{resId}")
    Call<List<Area>> getAreas(@Path("resId") Integer resId);

    @POST("api/areas")
    Call<Area> createArea(@Body Map<String, Object> data);

    // ==========================================
    // TABLES (Ban an) - is_occupied = red/green
    // ==========================================
    @GET("api/tables/area/{areaId}")
    Call<List<TableModel>> getTablesByArea(@Path("areaId") Integer areaId);

    @GET("api/tables/restaurant/{resId}")
    Call<List<TableModel>> getAllTablesByRestaurant(@Path("resId") Integer resId);

    @PUT("api/tables/{id}/status")
    Call<TableModel> updateTableStatus(@Path("id") Integer tableId,
            @Body Map<String, Boolean> data);

    @POST("api/tables")
    Call<TableModel> createTable(@Body Map<String, Object> data);

    // ==========================================
    // CATEGORIES & UNITS
    // ==========================================
    @GET("api/categories/restaurant/{resId}")
    Call<List<Category>> getCategories(@Path("resId") Integer resId);

    @POST("api/categories")
    Call<Category> createCategory(@Body Map<String, Object> data);

    @GET("api/units/restaurant/{resId}")
    Call<List<Unit>> getUnits(@Path("resId") Integer resId);

    @POST("api/units")
    Call<Unit> createUnit(@Body Map<String, Object> data);

    // ==========================================
    // MENU ITEMS (Mon an)
    // ==========================================
    @GET("api/menu-items/restaurant/{resId}")
    Call<List<MenuItem>> getMenuByRestaurant(@Path("resId") Integer resId, @retrofit2.http.Query("keyword") String keyword);

    @GET("api/menu-items/category/{catId}")
    Call<List<MenuItem>> getMenuByCategory(@Path("catId") Integer catId);

    @POST("api/menu-items")
    Call<MenuItem> createMenuItem(@Body Map<String, Object> data);

    @PUT("api/menu-items/{id}")
    Call<MenuItem> updateMenuItem(@Path("id") Integer id, @Body Map<String, Object> data);

    @retrofit2.http.Multipart
    @POST("api/upload/image")
    Call<Map<String, String>> uploadImage(@retrofit2.http.Part okhttp3.MultipartBody.Part file);

    // ==========================================
    // ORDERS (Hoa don & Nghiep vu chinh)
    // ==========================================

    /** Nhan vien mo ban - tao hoa don moi, ban -> co khach */
    @POST("api/orders/open")
    Call<Map> openTable(@Body Map<String, Integer> data);

    /** Lay hoa don dang phuc vu (status=0) cua 1 ban */
    @GET("api/orders/table/{tableId}/active")
    Call<Map> getActiveOrder(@Path("tableId") Integer tableId);

    /** Nhan vien them mon vao gio (status=0, co the sua) */
    @POST("api/orders/{orderId}/items")
    Call<OrderDetail> addItem(@Path("orderId") Integer orderId,
            @Body Map<String, Object> data);

    /** Lay chi tiet mon cua 1 hoa don */
    @GET("api/orders/{orderId}/details")
    Call<List<OrderDetail>> getOrderDetails(@Path("orderId") Integer orderId);

    /** Nhan vien gui mon -> status=1 (KHOA, nhan vien khong sua/xoa duoc) */
    @PUT("api/orders/{orderId}/send")
    Call<Map> sendOrder(@Path("orderId") Integer orderId);

    /** QUAN LY huy mon da gui (status 1 -> 2) */
    @PUT("api/orders/details/{detailId}/cancel")
    Call<Map> cancelItem(@Path("detailId") Integer detailId,
            @Body Map<String, Integer> data);

    /** Thanh toan: tinh tong, dong hoa don, ban -> trong */
    @POST("api/orders/{orderId}/checkout")
    Call<Map> checkout(@Path("orderId") Integer orderId);
}
