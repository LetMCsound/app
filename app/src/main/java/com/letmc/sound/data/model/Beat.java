package com.letmc.sound.data.model;
import com.google.gson.annotations.SerializedName;
public class Beat {
    public String id, title, genre, bpm, key, description;
    public double price;
    @SerializedName("audio_url")   public String audioUrl;
    @SerializedName("cover_url")   public String coverUrl;
    @SerializedName("musician_id") public String musicianId;
    @SerializedName("plays_count") public int playsCount;
    @SerializedName("likes_count") public int likesCount;
    @SerializedName("created_at")  public String createdAt;
    public Musician musician;
}
