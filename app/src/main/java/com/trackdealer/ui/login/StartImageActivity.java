package com.trackdealer.ui.login;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.trackdealer.BaseApp;
import com.trackdealer.R;
import com.trackdealer.net.Restapi;
import com.trackdealer.ui.main.MainActivity;
import com.trackdealer.ui.mvp.UserSettingsPresenter;
import com.trackdealer.ui.mvp.UserSettingsView;
import com.trackdealer.utils.ErrorHandler;
import com.trackdealer.utils.Prefs;
import com.trackdealer.utils.StaticUtils;

import java.util.Set;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Retrofit;

/**
 * Created by grechnev-av on 01.09.2017.
 */

public class StartImageActivity extends AppCompatActivity implements UserSettingsView {

    private static int SPLASH_TIME_OUT = 1500;

    @Bind(R.id.logo_image_view)
    ImageView imageView;
    @Bind(R.id.start_image_background)
    ImageView backgroundImageView;

    @Inject
    Retrofit retrofit;
    private Restapi restapi;

    UserSettingsPresenter userSettingsPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_image);
        ButterKnife.bind(this);

        ((BaseApp) getApplication()).getNetComponent().inject(this);
        restapi = retrofit.create(Restapi.class);

        userSettingsPresenter = new UserSettingsPresenter(restapi, this);
        userSettingsPresenter.attachView(this);

        backgroundImageView.setImageBitmap(StaticUtils.cutPicture(this, R.drawable.prelogin_background));
//        backgroundImageView.setImageResource(R.drawable.selector_prelogin_background);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.back_image);
        animation.setDuration(SPLASH_TIME_OUT);
        backgroundImageView.startAnimation(animation);

        Set<String> preferences = Prefs.getStringSet(this, "User-Cookie", "Cookies");
        if (preferences.isEmpty()) {
            new Handler().postDelayed(() -> {
                startActivity(new Intent(StartImageActivity.this, PreloginActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }, SPLASH_TIME_OUT);
        } else {
            userSettingsPresenter.getUserSeettings(SPLASH_TIME_OUT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userSettingsPresenter.detachView();
    }

    @Override
    public void getUserSettingsSuccess() {
        startMainActivity();
    }

    @Override
    public void getUserSettingsFailed(String error) {
        ErrorHandler.showToast(this, error);
        startActivity(new Intent(StartImageActivity.this, PreloginActivity.class));
        finish();
    }

    public void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
    }

    public void createAnim() {
        final float[] from = new float[3],
                to = new float[3];

        Color.colorToHSV(Color.parseColor("#FFFFFFFF"), from);   // from white
        Color.colorToHSV(Color.parseColor("#FF007cd0"), to);     // to red

        ValueAnimator anim = ValueAnimator.ofFloat(0, 1);   // animate from 0 to 1
        anim.setDuration(SPLASH_TIME_OUT);                              // for 300 ms

        final float[] hsv = new float[3];                  // transition color
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // Transition along each axis of HSV (hue, saturation, value)
                hsv[0] = from[0] + (to[0] - from[0]) * animation.getAnimatedFraction();
                hsv[1] = from[1] + (to[1] - from[1]) * animation.getAnimatedFraction();
                hsv[2] = from[2] + (to[2] - from[2]) * animation.getAnimatedFraction();

                imageView.setColorFilter(Color.HSVToColor(hsv));
            }
        });

        anim.start();
    }
}
