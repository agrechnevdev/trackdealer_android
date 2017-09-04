package com.trackdealer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.trackdealer.BaseApp;
import com.trackdealer.R;
import com.trackdealer.net.Restapi;
import com.trackdealer.utils.Prefs;

import java.util.Set;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = "LoginActivity";

    @Inject
    Retrofit retrofit;
    private Restapi restapi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Set<String> preferences = Prefs.getStringSet(this, "User-Cookie", "Cookies");
//        if (preferences.isEmpty()) {
        setContentView(R.layout.activity_login);
        ((BaseApp) getApplication()).getNetComponent().inject(this);
        restapi = retrofit.create(Restapi.class);
        ButterKnife.bind(this);
        restapi = retrofit.create(Restapi.class);

//        } else {
//            Intent intent = new Intent(getApplicationContext(), CoreActivity.class);
//            startActivity(intent);
//            finish();
//        }
    }

    @OnClick(R.id.login_btn_login)
    public void clickLogin() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}
