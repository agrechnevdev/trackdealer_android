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

import com.trackdealer.R;
import com.trackdealer.base.BaseActivity;
import com.trackdealer.utils.StaticUtils;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 202);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 202: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    new LovelyStandardDialog(this, LovelyStandardDialog.ButtonLayout.HORIZONTAL)
                            .setTopColorRes(R.color.colorLightBlue)
                            .setButtonsColorRes(R.color.colorOrange)
                            .setIcon(R.drawable.ic_warning_red)
                            .setTitle(R.string.permisstion_needed_title)
                            .setMessage(R.string.permisstion_needed_text_2)
                            .setPositiveButton(R.string.positive, v -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 202))
                            .setNegativeButton(R.string.negative, null)
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
