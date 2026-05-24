package com.letmc.sound.ui.auth;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.letmc.sound.R;

public class AuthActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_container, new LoginFragment())
                .commit();
        }
    }
}
