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
import com.trackdealer.models.User;
import com.trackdealer.models.UserSettings;
import com.trackdealer.net.Restapi;
import com.trackdealer.ui.main.MainActivity;
import com.trackdealer.ui.mvp.LoginPresenter;
import com.trackdealer.ui.mvp.LoginView;
import com.trackdealer.ui.mvp.UserSettingsPresenter;
import com.trackdealer.ui.mvp.UserSettingsView;
import com.trackdealer.utils.ErrorHandler;
import com.trackdealer.utils.Prefs;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_USER_DATA;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_USER;

public class LoginActivity extends BaseActivity implements LoginView, UserSettingsView {

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

    LoginPresenter loginPresenter;
    UserSettingsPresenter userSettingsPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((BaseApp) getApplication()).getNetComponent().inject(this);
        restapi = retrofit.create(Restapi.class);

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.singin);
        }

        subscription = new CompositeDisposable();
        initSubscribtion();

        loginPresenter = new LoginPresenter(restapi, this);
        loginPresenter.attachView(this);

        userSettingsPresenter = new UserSettingsPresenter(restapi, this);
        userSettingsPresenter.attachView(this);

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
        if (loginPresenter != null)
            loginPresenter.detachView();
        if (userSettingsPresenter != null)
            userSettingsPresenter.detachView();
        subscription.dispose();
    }

    @OnClick(R.id.login_btn_login)
    public void clickLogin() {
        showProgressBar();
        loginPresenter.login(textLogin.getText().toString(), textPassword.getText().toString());
    }

    @Override
    public void loginFailed(String error) {
        hideProgressBar();
        ErrorHandler.showToast(this, error);
    }

    public void loginSuccess() {
        userSettingsPresenter.getUserSeettings(0);
    }

    @Override
    public void getUserSettingsSuccess(UserSettings userSettings) {
        hideProgressBar();
        Prefs.putUser(getApplicationContext(), SHARED_FILENAME_USER_DATA, SHARED_KEY_USER, new User(userSettings.getUsername(), userSettings.getName(), userSettings.getStatus()));
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void getUserSettingsFailed(String error) {
        hideProgressBar();
        ErrorHandler.showToast(this, error);
    }

    protected void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    protected void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }


}
