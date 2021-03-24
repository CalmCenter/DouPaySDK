package me.doupay.sdk.net;


import com.google.gson.GsonBuilder;

import me.doupay.sdk.Constants;
import me.doupay.sdk.net.interceptors.Level;
import me.doupay.sdk.net.interceptors.LoggingInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.internal.platform.Platform;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;



public class RetrofitHelper {
    /**
     * 假设这个是默认的retrofit,因为一个App可能有多个baseUrl
     * 所以可能有多个retrofit
     */
    private static Retrofit retrofit;
    /**
     * 如果有其他url
     */
    private static Retrofit urlRetrofit;

    private static Retrofit.Builder builder;

    private static Retrofit.Builder getDefaultRetrofitBuilder() {
        if (builder == null) {
            builder = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());

        }
        return builder;
    }

    /**
     * 更灵活的获取retrofit方式
     */
    public static Retrofit getURLRetrofit(OkHttpClient client, String baseUrl) {
        if (urlRetrofit == null) {
            urlRetrofit = getDefaultRetrofitBuilder()
                .client(client)
                .baseUrl(baseUrl)
                .build();
        }
        return urlRetrofit;
    }

    public Retrofit getINSTANCE(boolean addSSL) {
        OkHttpClient client = OkhttpHelper.getDefaultClient(addSSL, new LoggingInterceptor.Builder().loggable(true)
            .setLevel(Level.BASIC)
            .log(Platform.INFO)
            .request("DouPaySDKRequest")
            .response("DouPaySDKResponse")
            .addHeader("Content-Type", "application/json;charset=UTF-8")
            .addHeader("X-Version", "v1.0")
            .addHeader("X-Language", Constants.language.getLanguage())
            .build());
        return new Retrofit.Builder().baseUrl(Constants.basrUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(client)
            .build();
    }
}
