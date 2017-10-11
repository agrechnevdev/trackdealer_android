package com.trackdealer.ui.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.trackdealer.R;
import com.trackdealer.base.BaseActivity;
import com.trackdealer.helpersUI.BitmapTransform;
import com.trackdealer.utils.Prefs;

import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by grechnev-av on 11.10.2017.
 */

public class PreloginActivity extends BaseActivity {

    private final String TAG = "PreloginActivity";

    @Bind(R.id.prelogin_btn_login)
    Button butLogin;

    @Bind(R.id.prelogin_background)
    ImageView imageViewBackground;

    @Bind(R.id.progressbar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Set<String> preferences = Prefs.getStringSet(this, "User-Cookie", "Cookies");
//        if (preferences.isEmpty()) {
        setContentView(R.layout.activity_prelogin);
        ButterKnife.bind(this);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int screenHeight = metrics.heightPixels;
        int screenWidth = metrics.widthPixels;
        int resourceStatus = getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = getResources().getDimensionPixelSize(resourceStatus);

        BitmapTransform bitmapTransform = new BitmapTransform(screenWidth, screenHeight, 0 ,screenHeight - statusBarHeight);
        Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.prelogin_background);
        imageViewBackground.setImageBitmap(bitmapTransform.transform(background));
//        } else {
//            Intent intent = new Intent(getApplicationContext(), CoreActivity.class);
//            startActivity(intent);
//            finish();
//        }
    }

    @OnClick(R.id.prelogin_btn_login)
    public void clickLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    @OnClick(R.id.prelogin_show_log)
    public void clickShowLog() {
        startActivity(new Intent(this, LogActivity.class));
    }
}
