package com.letmc.sound.data.api;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseManager {
    public static final String SUPABASE_URL  = "https://pownnyycfgfkstuvpdaw.supabase.co";
    public static final String SUPABASE_ANON = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InBvd25ueXljZmdma3N0dXZwZGF3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI0NjY1MzEsImV4cCI6MjA4ODA0MjUzMX0.G8_YSS3_xnqDucBAC7TFulLZwB0aQtxigzP7osTEMKI";

    private static Retrofit retrofit;
    private static String accessToken = null;

    public interface OnTokenExpiredListener { void onTokenExpired(); }
    private static OnTokenExpiredListener tokenExpiredListener = null;
    public static void setOnTokenExpiredListener(OnTokenExpiredListener l) { tokenExpiredListener = l; }

    public static Retrofit getClient() {
        if (retrofit != null) return retrofit;
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                okhttp3.Request.Builder req = chain.request().newBuilder()
                    .header("apikey", SUPABASE_ANON)
                    .header("Content-Type", "application/json");
                String token = accessToken != null ? accessToken : SUPABASE_ANON;
                req.header("Authorization", "Bearer " + token);
                Response response = chain.proceed(req.build());

                // Si el JWT expiró, limpiar token, notificar y reintentar con anon key
                if (response.code() == 401 && accessToken != null) {
                    response.close();
                    accessToken = null;
                    retrofit = null;
                    if (tokenExpiredListener != null) tokenExpiredListener.onTokenExpired();
                    okhttp3.Request retry = chain.request().newBuilder()
                        .header("apikey", SUPABASE_ANON)
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + SUPABASE_ANON)
                        .build();
                    return chain.proceed(retry);
                }
                return response;
            })
            .addInterceptor(logging).build();
        retrofit = new Retrofit.Builder()
            .baseUrl(SUPABASE_URL + "/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        return retrofit;
    }

    public static void setAccessToken(String token) { accessToken = token; retrofit = null; }
    public static String getAccessToken() { return accessToken; }
    public static boolean isLoggedIn() { return accessToken != null; }
    public static <T> T createService(Class<T> cls) { return getClient().create(cls); }
}
