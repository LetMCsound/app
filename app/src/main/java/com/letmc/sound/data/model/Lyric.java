package com.letmc.sound.data.model;
import com.google.gson.annotations.SerializedName;
public class Lyric {
    public String id, title, genre, snippet, description;
    public double price;
    @SerializedName("musician_id") public String musicianId;
    @SerializedName("created_at")  public String createdAt;
    public Musician musician;
}
