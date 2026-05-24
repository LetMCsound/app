package com.letmc.sound.data.model;
import com.google.gson.annotations.SerializedName;
public class Message {
    public String id, content, type;
    @SerializedName("conversation_id") public String conversationId;
    @SerializedName("sender_id")       public String senderId;
    @SerializedName("created_at")      public String createdAt;
    public Musician sender;
}
