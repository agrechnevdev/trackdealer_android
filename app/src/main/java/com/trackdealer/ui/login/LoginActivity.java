package com.trackdealer.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.trackdealer.BaseApp;
import com.trackdealer.R;
import com.trackdealer.base.BaseActivity;
import com.trackdealer.models.RMessage;
import com.trackdealer.models.User;
import com.trackdealer.net.Restapi;
import com.trackdealer.ui.main.MainActivity;
import com.trackdealer.utils.ConnectionsManager;
import com.trackdealer.utils.ErrorHandler;
import com.trackdealer.utils.Prefs;

import java.util.Set;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import timber.log.Timber;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_USER_DATA;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_USER;

public class LoginActivity extends BaseActivity {

    private final String TAG = "LoginActivity";

    @Inject
    Retrofit retrofit;
    private Restapi restapi;

    @Bind(R.id.login_lay_main)
    RelativeLayout layMain;

    CompositeDisposable subscription;

    @Bind(R.id.login_text_password)
    EditText textPassword;

    @Bind(R.id.login_text_login)
    EditText textLogin;

    @Bind(R.id.login_btn_login)
    Button butLogin;

    @Bind(R.id.progressbar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Set<String> preferences = Prefs.getStringSet(this, "User-Cookie", "Cookies");
        subscription = new CompositeDisposable();
        if (preferences.isEmpty()) {
            setContentView(R.layout.activity_login);
            ((BaseApp) getApplication()).getNetComponent().inject(this);
            restapi = retrofit.create(Restapi.class);
            ButterKnife.bind(this);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.singin);
            }
            initSubscribtion();
//        getMetrics();
//        Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.login_background);
//        imageView.setImageBitmap(bitmapTransform.transform(background));

        } else {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void initSubscribtion() {

        subscription.add(Observable.combineLatest(
                RxTextView.textChanges(textLogin).observeOn(AndroidSchedulers.mainThread()),
                RxTextView.textChanges(textPassword).observeOn(AndroidSchedulers.mainThread()),
                (register, pass) -> !TextUtils.isEmpty(register) && !TextUtils.isEmpty(pass))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::buttonEnabledState)
        );
    }

    public void buttonEnabledState(boolean enabled) {
        butLogin.setEnabled(enabled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscription.dispose();
    }

    @OnClick(R.id.login_btn_login)
    public void clickLogin() {
        showProgressBar();
        login();
//        loginSuccess(new RMessage("TRACKDEALER"));
    }


    void login() {

        if (ConnectionsManager.isOnline(this)) {
            subscription.add(
//                    FakeRestApi.login(this, textLogin.getText().toString(), textPassword.getText().toString())
                    restapi.login(textLogin.getText().toString(), textPassword.getText().toString())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(response -> {
                                        Timber.e(TAG + " register response code: " + response.code());
                                        if (response.isSuccessful()) {
                                            loginSuccess(response.body());
                                        } else {
                                            ErrorHandler.showToast(this, ErrorHandler.getErrorMessageFromResponse(response));
                                            hideProgressBar();
                                        }
                                    },
                                    ex -> {
                                        Timber.e(ex, TAG + " register onError() " + ex.getMessage());
                                        ErrorHandler.showToast(this, ErrorHandler.DEFAULT_SERVER_ERROR_MESSAGE);
                                        hideProgressBar();
                                    }
                            ));
        } else {
            hideProgressBar();
            ErrorHandler.showToast(this, ErrorHandler.DEFAULT_NETWORK_ERROR_MESSAGE_SHORT);
        }
    }

    public void loginSuccess(RMessage rMessage) {
        hideProgressBar();
        Prefs.putUser(getApplicationContext(), SHARED_FILENAME_USER_DATA, SHARED_KEY_USER, new User(textLogin.getText().toString(), null, rMessage.getMessage()));
//        Prefs.putString(getApplicationContext(), SHARED_FILENAME_USER_DATA, SHARED_KEY_PASSWORD, textPassword.getText().toString());
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    protected void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    protected void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }


}
