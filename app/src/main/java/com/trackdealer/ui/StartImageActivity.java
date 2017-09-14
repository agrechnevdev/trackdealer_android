package com.trackdealer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.trackdealer.R;

/**
 * Created by grechnev-av on 01.09.2017.
 */

public class StartImageActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_logo);
        new Handler().postDelayed(() -> {
            startActivity(new Intent(StartImageActivity.this, LoginActivity.class));
            overridePendingTransition(0, 0);
            finish();
        }, SPLASH_TIME_OUT);
    }
}
