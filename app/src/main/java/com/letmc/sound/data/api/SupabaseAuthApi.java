package com.letmc.sound.data.api;

import com.letmc.sound.data.model.AuthResponse;
import com.letmc.sound.data.model.LoginRequest;
import com.letmc.sound.data.model.RegisterRequest;
import retrofit2.Call;
import retrofit2.http.*;

public interface SupabaseAuthApi {
    @POST("auth/v1/signup")
    Call<AuthResponse> register(@Body RegisterRequest body);

    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> login(@Body LoginRequest body);
}
