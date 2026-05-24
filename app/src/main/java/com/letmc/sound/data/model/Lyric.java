package com.letmc.sound.data.model;
import com.google.gson.annotations.SerializedName;

public class Lyric {
    public String id, title, genre, language, content, description;

    @SerializedName("seller_id")       public String sellerId;
    @SerializedName("seller_name")     public String sellerName;
    @SerializedName("cover_url")       public String coverUrl;
    @SerializedName("price_standard")  public double priceStandard;
    @SerializedName("price_premium")   public double pricePremium;
    @SerializedName("price_exclusive") public double priceExclusive;
    @SerializedName("is_published")    public boolean isPublished;
    @SerializedName("created_at")      public String createdAt;
}
