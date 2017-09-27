package com.trackdealer.ui;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.trackdealer.R;
import com.trackdealer.ui.login.LoginActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by grechnev-av on 01.09.2017.
 */

public class StartImageActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 2000;

    @Bind(R.id.logo_image_view)
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_logo);
        ButterKnife.bind(this);
        createAnim();
        new Handler().postDelayed(() -> {
            startActivity(new Intent(StartImageActivity.this, LoginActivity.class));
            overridePendingTransition(0, 0);
            finish();
        }, SPLASH_TIME_OUT);
    }

    public void createAnim(){
        final float[] from = new float[3],
                to =   new float[3];

        Color.colorToHSV(Color.parseColor("#FFFFFFFF"), from);   // from white
        Color.colorToHSV(Color.parseColor("#FFFF0000"), to);     // to red

        ValueAnimator anim = ValueAnimator.ofFloat(0, 1);   // animate from 0 to 1
        anim.setDuration(2000);                              // for 300 ms

        final float[] hsv  = new float[3];                  // transition color
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                // Transition along each axis of HSV (hue, saturation, value)
                hsv[0] = from[0] + (to[0] - from[0])*animation.getAnimatedFraction();
                hsv[1] = from[1] + (to[1] - from[1])*animation.getAnimatedFraction();
                hsv[2] = from[2] + (to[2] - from[2])*animation.getAnimatedFraction();

                imageView.setColorFilter(Color.HSVToColor(hsv));
            }
        });

        anim.start();
    }
}
