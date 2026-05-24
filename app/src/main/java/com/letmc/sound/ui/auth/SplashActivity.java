package com.letmc.sound.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.letmc.sound.MainActivity;
import com.letmc.sound.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager session = new SessionManager(this);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = session.isLoggedIn()
                ? new Intent(this, MainActivity.class)
                : new Intent(this, AuthActivity.class);
            startActivity(intent);
            finish();
        }, 1500);
    }
}
