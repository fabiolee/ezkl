package com.fabiolee.architecture.mvp.model.remote;

import com.fabiolee.architecture.mvp.model.bean.SendMessageRequest;
import com.fabiolee.architecture.mvp.model.bean.SendMessageResponse;

import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

/**
 * @author fabio.lee
 */
public interface SmsService {
    String BASE_URL = "https://developer.tm.com.my:8443/SMSSBV1/SMSImpl/SMSImplRS/";

    @POST("SendMessage")
    Observable<SendMessageResponse> sendMessage(@Body SendMessageRequest request);
}
