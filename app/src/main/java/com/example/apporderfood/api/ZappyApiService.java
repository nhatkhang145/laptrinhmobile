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
import retrofit2.http.Query;

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

    // Thêm bàn (Gửi đi một cục Map)
    @POST("api/tables")
    Call<TableModel> createTable(@Body Map<String, Object> bodyData);

    // Cập nhật tên bàn
    @PUT("api/tables/{id}")
    Call<TableModel> updateTable(@Path("id") Integer tableId, @Body Map<String, Object> bodyData);

    @DELETE("api/tables/{id}")
    Call<Map> deleteTable(@Path("id") Integer id);

    // ==========================================
    // CATEGORIES & UNITS
    // ==========================================
    @GET("api/categories/restaurant/{resId}")
    Call<List<Category>> getCategories(@Path("resId") Integer resId);

    // Thêm danh mục
    @POST("api/categories")
    Call<Category> createCategory(@Body Map<String, Object> bodyData);

    // Cập nhật danh mục
    @PUT("api/categories/{id}")
    Call<Category> updateCategory(@Path("id") Integer id, @Body Map<String, Object> bodyData);

    // Xóa danh mục
    @DELETE("api/categories/{id}")
    Call<Map> deleteCategory(@Path("id") Integer id);

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

    /** Nhan vien gui nhieu mon vao gio (batch) */
    @POST("api/orders/{orderId}/items/batch")
    Call<Map<String, String>> addBatchItems(@Path("orderId") Integer orderId,
            @Body java.util.List<Map<String, Object>> itemsData);

    /** Lay chi tiet mon cua 1 hoa don */
    @GET("api/orders/{orderId}/details")
    Call<List<OrderDetail>> getOrderDetails(@Path("orderId") Integer orderId);

    /** Lay danh sach tat ca hoa don dang phuc vu cua nha hang */
    @GET("api/orders/restaurant/{resId}/active")
    Call<List<Map<String, Object>>> getActiveOrdersByRestaurant(@Path("resId") int resId);

    @GET("api/orders/restaurant/{resId}/paid")
    Call<List<Map<String, Object>>> getPaidOrdersByRestaurant(
            @Path("resId") int resId,
            @Query("fromDate") String fromDate,
            @Query("toDate") String toDate
    );

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
    /** Quen mat khau: gui ma otp*/
    @POST("api/auth/forgot-password/send-otp")
    Call<Map<String, String>> sendOtp(@Body Map<String, String> data);
    /** Quen mat khau: Kiem tra ma otp */
    @POST("api/auth/forgot-password/verify-otp")
    Call<Map<String, String>> verifyOtp(@Body Map<String, String> data);
    /** Quen mat khau: Reset password, tao password moi*/
    @POST("api/auth/forgot-password/reset-password")
    Call<Map<String, String>> resetPassword(@Body Map<String, String> data);

    /**Chỉnh sửa thông tin nhân viên**/
    @PUT("api/users/{id}")
    Call<User> updateUser(@Path("id") Integer id, @Body Map<String, Object> data);
    /** Dang ky tai khoan: Gui ma otp xac nhan gmail */
    @POST("api/auth/register/send-otp")
    Call<Map<String, String>> sendRegisterOtp(@Body Map<String, String> body);
    /** Dang ky tai khoan: Xac nhan otp vua gui */
    @POST("api/auth/register/verify-otp")
    Call<Map<String, String>> verifyRegisterOtp(@Body Map<String, String> body);

    // ==========================================
    // USER (NHÂN VIÊN)
    // ==========================================
    @GET("api/users/restaurant/{resId}")
    Call<List<com.example.apporderfood.model.User>> getUsersByRestaurant(@Path("resId") int resId);

    // ==========================================
    // SHIFT (CA LÀM VIỆC)
    // ==========================================

    @GET("api/shifts/restaurant/{resId}")
    Call<List<Map<String, Object>>> getShiftHistory(@Path("resId") int resId);

    @GET("api/shifts/restaurant/{resId}/active")
    Call<Map<String, Object>> getActiveShift(@Path("resId") int resId);

    @POST("api/shifts/open")
    Call<Map<String, Object>> openShift(@Body Map<String, Object> shiftData);

    @PUT("api/shifts/{id}/employees")
    Call<Map<String, Object>> updateShiftEmployees(@Path("id") int shiftId, @Body Map<String, Object> shiftData);

    @PUT("api/shifts/{id}/close")
    Call<Map<String, Object>> closeShift(@Path("id") int shiftId, @Body Map<String, Object> shiftData);
    // ==========================================
    // THỐNG KÊ (STATS)
    // ==========================================
    @GET("api/orders/stats/restaurant/{resId}")
    Call<Map<String, Object>> getDashboardStats(
            @Path("resId") int resId,
            @retrofit2.http.Query("period") String period
    );
    //=======================
    // LogOut
    //=========================
    @PUT("api/users/{id}/logout")
    Call<Map<String, String>> logoutUser(@Path("id") Integer id);
}
