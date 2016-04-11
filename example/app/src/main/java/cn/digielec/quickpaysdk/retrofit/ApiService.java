package cn.digielec.quickpaysdk.retrofit;

import com.squareup.okhttp.RequestBody;


import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.POST;


/**
 * Created by dw on 2016/3/31.
 */
public interface ApiService {
    final String BASE_URL = "https://api.mch.weixin.qq.com/";


    @POST("pay/unifiedorder")
        Call<String> createWeChatPayOrder(@Body RequestBody data);

    @POST("createorder/pay_wap.php")
    Call<String> createBaiDuPayOrder(@Body RequestBody data);


}
