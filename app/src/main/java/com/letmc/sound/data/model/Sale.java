package com.letmc.sound.data.model;
import com.google.gson.annotations.SerializedName;
public class Sale {
    public String id, status;
    @SerializedName("beat_id")    public String beatId;
    @SerializedName("buyer_id")   public String buyerId;
    @SerializedName("seller_id")  public String sellerId;
    public double amount;
    @SerializedName("created_at") public String createdAt;
    public Beat beat;
}
