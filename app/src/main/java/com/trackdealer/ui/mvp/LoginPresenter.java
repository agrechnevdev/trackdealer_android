package com.trackdealer.ui.mvp;

import android.content.Context;

import com.trackdealer.R;
import com.trackdealer.base.BasePresenter;
import com.trackdealer.net.Restapi;
import com.trackdealer.utils.ConnectionsManager;
import com.trackdealer.utils.ErrorHandler;

import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by anton on 03.12.2017.
 */

public class LoginPresenter extends BasePresenter<LoginView> {

    private final String TAG = "LoginPresenter ";

    private CompositeDisposable subscription;
    private LoginView loginView;
    private final Restapi restapi;
    private Context context;

    public LoginPresenter(Restapi restapi, Context context) {
        subscription = new CompositeDisposable();
        this.context = context;
        this.restapi = restapi;
    }

    @Override
    public void attachView(LoginView loginView) {
        this.loginView = loginView;
    }

    @Override
    public void detachView() {
        loginView = null;
        subscription.dispose();
    }

    public void login(String login, String password) {

        if (ConnectionsManager.isOnline(context)) {
            subscription.add(
//                    FakeRestApi.login(this, textLogin.getText().toString(), textPassword.getText().toString())
                    restapi.login(login, password)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(response -> {
                                        Timber.e(TAG + " register response code: " + response.code());
                                        if (response.isSuccessful()) {
                                            loginView.loginSuccess();
                                        } else {
                                            loginView.loginFailed(ErrorHandler.getErrorMessageFromResponse(context, response));
                                        }
                                    },
                                    ex -> {
                                        Timber.e(ex, TAG + " register onError() " + ex.getMessage());
                                        loginView.loginFailed(ErrorHandler.buildErrorDescriptionShort(context, ex));
                                    }
                            ));
        } else {
            loginView.loginFailed(context.getString(R.string.default_network_error));
        }
    }



}
