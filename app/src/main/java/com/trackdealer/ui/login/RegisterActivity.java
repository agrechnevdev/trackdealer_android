package com.trackdealer.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
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
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import timber.log.Timber;

/**
 * Created by grechnev-av on 07.11.2017.
 */

public class RegisterActivity extends BaseActivity {

    private final String TAG = "RegisterActivity";

    @Inject
    Retrofit retrofit;
    private Restapi restapi;

    @Bind(R.id.register_lay_main)
    RelativeLayout layMain;

    CompositeDisposable subscription;


    @Bind(R.id.register_text_login)
    EditText textLogin;
    @Bind(R.id.register_text_password)
    EditText textPassword;
    @Bind(R.id.register_text_repeat_password)
    EditText textPassword2;
    @Bind(R.id.register_text_email)
    EditText textEmail;
    @Bind(R.id.register_text_name)
    EditText textName;

    @Bind(R.id.register_lay_text_password)
    TextInputLayout textLayPassword;
    @Bind(R.id.register_lay_text_repeat_password)
    TextInputLayout textLayPassword2;

    @Bind(R.id.register_btn_continue)
    Button butContinue;

    @Bind(R.id.progressbar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Set<String> preferences = Prefs.getStringSet(this, "User-Cookie", "Cookies");
//        if (preferences.isEmpty()) {
        setContentView(R.layout.activity_register);
        ((BaseApp) getApplication()).getNetComponent().inject(this);
        restapi = retrofit.create(Restapi.class);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.registration);
        }
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

        Observable<CharSequence> observablePass = RxTextView.textChanges(textPassword).skip(1);
        Observable<CharSequence> observablePass2 = RxTextView.textChanges(textPassword2).skip(1);

        subscription.add(Observable.combineLatest(
                observablePass, observablePass2, (pass, pass2) ->
                        pass.toString().equals(pass2.toString()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(ebable -> {
                    if(ebable){
                        textLayPassword2.setErrorEnabled(false);
                    } else {
                        textLayPassword2.setError("Пароли не совпадают!");
                    }

                })
        );

        subscription.add(Observable.combineLatest(
                RxTextView.textChanges(textLogin), observablePass,
                observablePass2 , RxTextView.textChanges(textName),
                RxTextView.textChanges(textEmail),
                (login, pass, pass2, name, email) -> !TextUtils.isEmpty(login) && !TextUtils.isEmpty(pass) &&
                        !TextUtils.isEmpty(pass2) && !TextUtils.isEmpty(name) &&
                        pass.length() > 3 && pass2.length() > 3
                        && pass.toString().equals(pass2.toString()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(this::buttonEnabledState)
        );
    }

    public void buttonEnabledState(boolean enabled) {
        butContinue.setEnabled(enabled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscription.dispose();
    }

    @OnClick(R.id.register_btn_continue)
    public void clickRegister() {
        showProgressBar();
        register();
    }


    void register() {

        if (ConnectionsManager.isOnline(this)) {
            subscription.add(FakeRestApi.register(this, textLogin.getText().toString(), textPassword.getText().toString(),
                    textName.getText().toString(), textEmail.getText().toString())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(response -> {
                                Timber.e(TAG + " register response code: " + response.code());
                                if (response.isSuccessful()) {
                                    registerSuccess();
                                } else {
                                    ErrorHandler.showSnackbarError(layMain, ErrorHandler.getErrorMessageFromResponse(response));
                                    hideProgressBar();
                                }
                            },
                            ex -> {
                                Timber.e(ex, TAG + " register onError() " + ex.getMessage());
                                ErrorHandler.showSnackbarError(layMain, ErrorHandler.DEFAULT_SERVER_ERROR_MESSAGE);
                                hideProgressBar();
                            }
                    ));
        } else {
            hideProgressBar();
            ErrorHandler.showSnackbarError(layMain, ErrorHandler.DEFAULT_NETWORK_ERROR_MESSAGE_SHORT);
        }
    }

    public void registerSuccess() {
        hideProgressBar();
        Intent intent = new Intent(this, LoginActivity.class);
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
