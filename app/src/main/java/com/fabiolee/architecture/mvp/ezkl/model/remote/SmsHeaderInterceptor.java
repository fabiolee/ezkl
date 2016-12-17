package com.fabiolee.architecture.mvp.ezkl.model.remote;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author fabio.lee
 */
public class SmsHeaderInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request newRequest = request.newBuilder()
                .addHeader("APITokenId", "0BvjhrDd4XnXYu9L27Wf6EsiNcU=")
                .addHeader("PartnerId", "yye2fBhdGFKWpf+FuyU5ytdt2GQ=")
                .addHeader("PartnerTokenId", "uce4+H+d4gTFUJFNmMa+ejiLmhY=")
                .addHeader("Content-Type", "application/json")
                .build();
        return chain.proceed(newRequest);
    }
}
