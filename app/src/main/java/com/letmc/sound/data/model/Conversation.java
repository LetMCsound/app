package com.letmc.sound.data.model;
import com.google.gson.annotations.SerializedName;
public class Conversation {
    public String id, status, type;
    @SerializedName("buyer_id")   public String buyerId;
    @SerializedName("seller_id")  public String sellerId;
    @SerializedName("created_at") public String createdAt;
    @SerializedName("updated_at") public String updatedAt;
    public Musician buyer, seller;
    @SerializedName("last_message") public String lastMessage;
}
