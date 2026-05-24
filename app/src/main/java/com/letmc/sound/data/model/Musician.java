package com.letmc.sound.data.model;
import com.google.gson.annotations.SerializedName;
public class Musician {
    public String id, name, bio, location, slug;
    @SerializedName("user_id")    public String userId;
    @SerializedName("avatar_url") public String avatarUrl;
    @SerializedName("cover_url")  public String coverUrl;
    public String tags;
}
