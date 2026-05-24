package com.letmc.sound.data.model;
import com.google.gson.annotations.SerializedName;
public class AuthResponse {
    @SerializedName("access_token")  public String accessToken;
    @SerializedName("refresh_token") public String refreshToken;
    public User user;
}
