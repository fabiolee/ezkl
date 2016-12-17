package com.fabiolee.architecture.mvp.ezkl.model.bean;

import com.google.gson.annotations.SerializedName;

/**
 * @author fabio.lee
 */
public class SendMessageResponse {
    @SerializedName("responseResult")
    public String responseResult;
    @SerializedName("responseCode")
    public String responseCode;
}
