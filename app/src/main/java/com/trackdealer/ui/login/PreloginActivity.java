package com.trackdealer.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.trackdealer.R;
import com.trackdealer.base.BaseActivity;
import com.trackdealer.utils.Prefs;
import com.trackdealer.utils.StaticUtils;

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

    @Bind(R.id.logo_text)
    TextView logoText;

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

        imageViewBackground.setImageBitmap(StaticUtils.cutPicture(this, R.drawable.prelogin_background));
        String text = "<b><font color=#007cd0>TRACK</font></b><b><font color=#fd7d20>DEALER</font></b>";
        logoText.setText(Html.fromHtml(text));

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

    @OnClick(R.id.prelogin_btn_register)
    public void clickRegister() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    @OnClick(R.id.prelogin_show_log)
    public void clickShowLog() {
        startActivity(new Intent(this, LogActivity.class));
    }
}
