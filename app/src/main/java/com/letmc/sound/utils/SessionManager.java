package com.letmc.sound.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import com.letmc.sound.data.api.SupabaseManager;

public class SessionManager {
    private static final String PREF_NAME    = "letmc_session";
    private static final String KEY_TOKEN    = "access_token";
    private static final String KEY_USER_ID  = "user_id";
    private static final String KEY_EMAIL    = "email";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        SharedPreferences p;
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
            p = EncryptedSharedPreferences.create(context, PREF_NAME, masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (Exception e) {
            p = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
        prefs = p;
        // Restaurar token si existe
        String saved = prefs.getString(KEY_TOKEN, null);
        if (saved != null) SupabaseManager.setAccessToken(saved);
    }

    public void saveSession(String token, String userId, String email) {
        prefs.edit().putString(KEY_TOKEN, token)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_EMAIL, email).apply();
        SupabaseManager.setAccessToken(token);
    }

    public void clearSession() {
        prefs.edit().clear().apply();
        SupabaseManager.setAccessToken(null);
    }

    public boolean isLoggedIn()   { return prefs.getString(KEY_TOKEN, null) != null; }
    public String  getUserId()    { return prefs.getString(KEY_USER_ID, null); }
    public String  getEmail()     { return prefs.getString(KEY_EMAIL, null); }
    public String  getToken()     { return prefs.getString(KEY_TOKEN, null); }
}
