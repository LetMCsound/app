package com.letmc.sound.data.api;

import com.letmc.sound.data.model.*;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface SupabaseApi {

    // ── BEATS ───────────────────────────────────────────────────
    @GET("rest/v1/beats")
    Call<List<Beat>> getBeats(@Query("select") String select, @Query("order") String order, @Header("Range") String range);
    @GET("rest/v1/beats")
    Call<List<Beat>> getBeatById(@Query("select") String select, @Query("id") String idFilter);
    @GET("rest/v1/beats")
    Call<List<Beat>> getBeatsByMusician(@Query("select") String select, @Query("musician_id") String musicianFilter, @Query("order") String order);
    @POST("rest/v1/beats")
    @Headers("Prefer: return=minimal")
    Call<Void> insertBeat(@Body Map<String, Object> body);

    // ── LYRICS ──────────────────────────────────────────────────
    @GET("rest/v1/lyrics")
    Call<List<Lyric>> getLyrics(@Query("select") String select, @Query("order") String order, @Header("Range") String range);
    @GET("rest/v1/lyrics")
    Call<List<Lyric>> getLyricById(@Query("select") String select, @Query("id") String idFilter);
    @GET("rest/v1/lyrics")
    Call<List<Lyric>> getLyricsByMusician(@Query("select") String select, @Query("musician_id") String musicianFilter);
    @POST("rest/v1/lyrics")
    @Headers("Prefer: return=minimal")
    Call<Void> insertLyric(@Body Map<String, Object> body);

    // ── MUSICIANS ───────────────────────────────────────────────
    @GET("rest/v1/musicians")
    Call<List<Musician>> getMusicianById(@Query("select") String select, @Query("user_id") String userIdFilter);
    @GET("rest/v1/musicians")
    Call<List<Musician>> getMusicianBySlug(@Query("select") String select, @Query("slug") String slugFilter);
    @GET("rest/v1/musicians")
    Call<List<Musician>> getMusiciansByTag(@Query("select") String select, @Query("tags") String tagsFilter, @Query("order") String order);
    @PATCH("rest/v1/musicians")
    @Headers("Prefer: return=minimal")
    Call<Void> updateMusician(@Query("user_id") String userIdFilter, @Body Map<String, Object> body);

    // ── CONVERSATIONS ────────────────────────────────────────────
    @GET("rest/v1/conversations")
    Call<List<Conversation>> getConversations(@Query("select") String select, @Query("or") String orFilter, @Query("order") String order);
    @GET("rest/v1/conversations")
    Call<List<Conversation>> getConversationById(@Query("select") String select, @Query("id") String idFilter);
    @POST("rest/v1/conversations")
    Call<Conversation> createConversation(@Body Map<String, Object> body);
    @PATCH("rest/v1/conversations")
    @Headers("Prefer: return=minimal")
    Call<Void> updateConversation(@Query("id") String idFilter, @Body Map<String, Object> body);

    // ── MESSAGES ────────────────────────────────────────────────
    @GET("rest/v1/messages")
    Call<List<Message>> getMessages(@Query("select") String select, @Query("conversation_id") String convId, @Query("order") String order);
    @POST("rest/v1/messages")
    Call<Message> sendMessage(@Body Map<String, Object> body);

    // ── VENTAS ──────────────────────────────────────────────────
    @POST("rest/v1/ventas")
    @Headers("Prefer: return=minimal")
    Call<Void> createSale(@Body Map<String, Object> body);
    @GET("rest/v1/ventas")
    Call<List<Sale>> getSalesByBuyer(@Query("select") String select, @Query("buyer_id") String buyerFilter, @Query("order") String order);

    // ── NOTIFICATIONS ────────────────────────────────────────────
    @GET("rest/v1/notifications")
    Call<List<Notification>> getNotifications(@Query("select") String select, @Query("user_id") String userId, @Query("order") String order);
    @PATCH("rest/v1/notifications")
    @Headers("Prefer: return=minimal")
    Call<Void> markNotificationRead(@Query("id") String idFilter, @Body Map<String, Object> body);
    @PATCH("rest/v1/notifications")
    @Headers("Prefer: return=minimal")
    Call<Void> markAllNotificationsRead(@Query("user_id") String userIdFilter, @Body Map<String, Object> body);
}
