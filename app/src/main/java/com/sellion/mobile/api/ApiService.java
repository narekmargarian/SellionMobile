package com.sellion.mobile.api;

import com.sellion.mobile.model.CategoryGroupDto;
import com.sellion.mobile.model.ClientModel;
import com.sellion.mobile.entity.OrderEntity;
import com.sellion.mobile.model.Product;
import com.sellion.mobile.entity.ReturnEntity;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    // ИСПРАВЛЕНО: Сервер возвращает ApiResponse.ok(..., result)
    @GET("api/products/catalog")
    Call<ApiResponse<List<CategoryGroupDto>>> getCatalog();

    // ИСПРАВЛЕНО: ClientApiController возвращает чистый List (судя по вашему коду контроллера)
    // Но если вы добавите ApiResponse и туда — оберните в ApiResponse<List<ClientModel>>
    @GET("api/clients")
    Call<List<ClientModel>> getClients();

    @POST("api/orders/sync")
    Call<okhttp3.ResponseBody> sendOrders(@Body List<OrderEntity> orders);

    @POST("api/returns/sync")
    Call<okhttp3.ResponseBody> sendReturns(@Body List<ReturnEntity> returns);

    // ИСПРАВЛЕНО: ManagerApiController возвращает чистый List<String>
    @GET("api/public/managers")
    Call<List<String>> getManagersList();

    @GET("api/orders/manager/{managerId}/current-month")
    Call<List<OrderEntity>> getOrdersByManager(@Path("managerId") String managerId);

    @GET("api/returns/manager/{managerId}/current-month")
    Call<List<ReturnEntity>> getReturnsByManager(@Path("managerId") String managerId);

    @GET("api/public/managers/verify")
    Call<Map<String, String>> verifyKey(@Header("X-API-Key") String apiKey);
}
