package com.example.alertify_department_admin.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.example.alertify_department_admin.R;
import com.example.alertify_department_admin.main_utils.AppSharedPreferences;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        goToNextActivity();
    }

    private void goToNextActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AppSharedPreferences appSharedPreferences = new AppSharedPreferences(SplashActivity.this);
                boolean check = appSharedPreferences.getBoolean("depAdminLogin");
                Intent intent;

                if (check) {
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, 1000);
    }
}