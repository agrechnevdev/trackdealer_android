package com.trackdealer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.trackdealer.BaseApp;
import com.trackdealer.R;
import com.trackdealer.models.User;
import com.trackdealer.net.FakeRestApi;
import com.trackdealer.net.Restapi;
import com.trackdealer.utils.ConnectionsManager;
import com.trackdealer.utils.ErrorHandler;
import com.trackdealer.utils.Prefs;

import java.util.Set;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import timber.log.Timber;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_USER_DATA;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_PASSWORD;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_USER;

public class LoginActivity extends BaseActivity {

    private final String TAG = "LoginActivity";

    @Inject
    Retrofit retrofit;
    private Restapi restapi;

    @Bind(R.id.login_lay_main)
    RelativeLayout layMain;

    CompositeDisposable subscription;
    @Bind(R.id.login_back_imageview)
    ImageView imageView;

    @Bind(R.id.login_text_password)
    EditText textPassword;

    @Bind(R.id.login_text_login)
    EditText textLogin;

    @Bind(R.id.login_btn_login)
    Button butLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Set<String> preferences = Prefs.getStringSet(this, "User-Cookie", "Cookies");
//        if (preferences.isEmpty()) {
        setContentView(R.layout.activity_login);
        ((BaseApp) getApplication()).getNetComponent().inject(this);
        restapi = retrofit.create(Restapi.class);
        ButterKnife.bind(this);

        initSubscribtion();
//        getMetrics();
//        Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.login_background);
//        imageView.setImageBitmap(bitmapTransform.transform(background));

//        } else {
//            Intent intent = new Intent(getApplicationContext(), CoreActivity.class);
//            startActivity(intent);
//            finish();
//        }
    }

    public void initSubscribtion() {
        subscription = new CompositeDisposable();

//        subscription.add(Observable.combineLatest(
//                RxTextView.textChanges(textLogin).observeOn(AndroidSchedulers.mainThread()),
//                RxTextView.textChanges(textPassword).observeOn(AndroidSchedulers.mainThread()),
//               (login, pass) -> !TextUtils.isEmpty(login) && !TextUtils.isEmpty(pass))
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribeOn(Schedulers.io())
//                        .subscribe(this::buttonEnabledState)
//        );
    }

    public void buttonEnabledState(boolean enabled){
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
    }

    void login() {
        if (ConnectionsManager.isOnline(this)) {
            subscription.add(FakeRestApi.login(this)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(response -> {
                                Timber.e(TAG + " login response code: " + response.code());
                                if (response.isSuccessful()) {
                                    loginSuccess();
                                } else {
                                    showError(layMain, ErrorHandler.getErrorMessageFromResponse(response));
                                }
                            },
                            ex -> {
                                Timber.e(ex, TAG + " login onError() " + ex.getMessage());
                                showError(layMain, ErrorHandler.DEFAULT_SERVER_ERROR_MESSAGE);
                            }
                    ));
        } else {
            showError(layMain, ErrorHandler.DEFAULT_NETWORK_ERROR_MESSAGE_SHORT);
        }
    }

    public void loginSuccess() {
        hideProgressBar();
        Prefs.putUser(getApplicationContext(), SHARED_FILENAME_USER_DATA, SHARED_KEY_USER, new User(100, textLogin.getText().toString()));
        Prefs.putString(getApplicationContext(), SHARED_FILENAME_USER_DATA, SHARED_KEY_PASSWORD, textPassword.getText().toString());
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }


}
