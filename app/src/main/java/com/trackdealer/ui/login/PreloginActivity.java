package com.trackdealer.ui.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.trackdealer.BaseApp;
import com.trackdealer.R;
import com.trackdealer.base.BaseActivity;
import com.trackdealer.models.User;
import com.trackdealer.models.UserSettings;
import com.trackdealer.net.Restapi;
import com.trackdealer.ui.main.MainActivity;
import com.trackdealer.utils.ErrorHandler;
import com.trackdealer.utils.Prefs;
import com.trackdealer.utils.StaticUtils;

import java.util.Set;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Retrofit;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_USER_DATA;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_USER;

/**
 * Created by grechnev-av on 11.10.2017.
 */

public class PreloginActivity extends BaseActivity {

    private final String TAG = "PreloginActivity";


    @Bind(R.id.prelogin_btn_login)
    Button butLogin;

    @Bind(R.id.logo_text)
    TextView logoText;

    @Bind(R.id.prelogin_background)
    ImageView imageViewBackground;

    @Bind(R.id.progressbar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prelogin);
        ButterKnife.bind(this);

        imageViewBackground.setImageBitmap(StaticUtils.cutPicture(this, R.drawable.prelogin_background));
        String text = "<b><font color=#007cd0>TRACK</font></b><b><font color=#fd7d20>DEALER</font></b>";
        logoText.setText(Html.fromHtml(text));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 201);
        }
    }

    @OnClick(R.id.prelogin_btn_login)
    public void clickLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    @OnClick(R.id.prelogin_btn_register)
    public void clickRegister() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    @OnClick(R.id.prelogin_show_log)
    public void clickShowLog() {
        startActivity(new Intent(this, LogActivity.class));
    }

    protected void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    protected void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }
}
