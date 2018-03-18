package com.trackdealer.ui.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.trackdealer.R;
import com.trackdealer.base.BaseActivity;
import com.trackdealer.utils.StaticUtils;

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 201: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    new MaterialStyledDialog.Builder(this)
                            .setTitle(getString(R.string.permisstion_needed_title))
                            .setHeaderDrawable(R.drawable.app_logo_bold)
                            .setDescription(getString(R.string.permisstion_needed_text))
                            .setPositiveText(R.string.positive)
                            .onPositive((dialog, which) -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 201))
                            .setNegativeText(R.string.negative)
                            .onNegative((dialog, which) -> {
                            })
                            .show();
                }
            }
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

}
