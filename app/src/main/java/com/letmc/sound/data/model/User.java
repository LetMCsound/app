package com.letmc.sound.data.model;
import com.google.gson.annotations.SerializedName;
public class User {
    public String id;
    public String email;
    @SerializedName("created_at") public String createdAt;
}
