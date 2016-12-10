package com.fabiolee.architecture.mvp.model.bean;

import com.google.gson.annotations.SerializedName;

/**
 * @author fabio.lee
 */
public class SendMessageRequest {
    @SerializedName("username")
    public String username;
    @SerializedName("password")
    public String password;
    @SerializedName("msgtype")
    public String msgType;
    @SerializedName("message")
    public String message;
    @SerializedName("to")
    public String messageTo;
    @SerializedName("hashkey")
    public String hashKey;
    @SerializedName("filename")
    public String fileName;
    @SerializedName("transcid")
    public String transcId;
}
