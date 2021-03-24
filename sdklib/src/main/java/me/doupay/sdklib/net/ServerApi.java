package me.doupay.sdklib.net;

import java.util.Map;

import io.reactivex.Observable;
import me.doupay.sdklib.bean.ContractCoinVo;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface ServerApi {
    ServerApi SERVICE_API = new RetrofitHelper().getINSTANCE(false).create(ServerApi.class);

    @FormUrlEncoded
    @POST()
    Observable<BaseVo<ContractCoinVo>> test(@Url String url, @FieldMap Map<String, Object> body);


}
