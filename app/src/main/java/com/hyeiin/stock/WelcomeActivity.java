package com.hyeiin.stock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;

public class WelcomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button btnStart = findViewById(R.id.btnWelcomeStart);
        Button btnLogout = findViewById(R.id.btnWelcomeLogout);
        Button btnLogin = findViewById(R.id.btnWelcomeLogin);

        boolean loggedIn = FirebaseManager.isLoggedIn()
                && FirebaseManager.currentUser() != null
                && FirebaseManager.currentUser().isEmailVerified();

        if (loggedIn) {
            btnStart.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.GONE);
        } else {
            btnStart.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);
        }

        btnStart.setOnClickListener(v -> startActivity(new Intent(this, StoreSelectActivity.class)));

        btnLogout.setOnClickListener(v -> {
            FirebaseManager.auth().signOut();
            UserSession.clear();
            recreate();
        });

        btnLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            private long lastBackPressedAt = 0L;

            @Override
            public void handleOnBackPressed() {
                long now = System.currentTimeMillis();
                if (now - lastBackPressedAt < 2000L) {
                    finishAffinity();
                    return;
                }
                lastBackPressedAt = now;
                Toast.makeText(
                        WelcomeActivity.this,
                        "뒤로가기를 한 번 더 누르면 앱이 종료됩니다.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}
