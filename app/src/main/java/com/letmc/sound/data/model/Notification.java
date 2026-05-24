package com.letmc.sound.data.model;
import com.google.gson.annotations.SerializedName;
public class Notification {
    public String id, title, body, type;
    @SerializedName("user_id")    public String userId;
    @SerializedName("is_read")    public boolean isRead;
    @SerializedName("created_at") public String createdAt;
}
