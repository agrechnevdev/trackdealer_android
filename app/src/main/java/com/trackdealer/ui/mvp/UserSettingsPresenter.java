package com.trackdealer.ui.mvp;

import android.content.Context;

import com.trackdealer.base.BasePresenter;
import com.trackdealer.net.Restapi;
import com.trackdealer.utils.ConnectionsManager;
import com.trackdealer.utils.ErrorHandler;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by anton on 05.01.2018.
 */

public class UserSettingsPresenter extends BasePresenter<UserSettingsView> {

    private final String TAG = "UserSettingsPresenter ";

    private CompositeDisposable subscription;
    private UserSettingsView userSettingsView;
    private final Restapi restapi;
    private Context context;

    public UserSettingsPresenter(Restapi restapi, Context context) {
        subscription = new CompositeDisposable();
        this.context = context;
        this.restapi = restapi;
    }

    @Override
    public void attachView(UserSettingsView userSettingsView) {
        this.userSettingsView = userSettingsView;
    }

    @Override
    public void detachView() {
        userSettingsView = null;
        subscription.dispose();
    }

    public void getUserSeettings(int delay) {

        if (ConnectionsManager.isOnline(context)) {
            subscription.add(
//                    FakeRestApi.login(this, textLogin.getText().toString(), textPassword.getText().toString())
                    restapi.getUserSettings()
                            .delay(delay, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(response -> {
                                        Timber.e(TAG + " register response code: " + response.code());
                                        if (response.isSuccessful()) {
                                            userSettingsView.getUserSettingsSuccess(response.body());
                                        } else {
                                            userSettingsView.getUserSettingsFailed(ErrorHandler.getErrorMessageFromResponse(response));
                                        }
                                    },
                                    ex -> {
                                        Timber.e(ex, TAG + " register onError() " + ex.getMessage());
                                        userSettingsView.getUserSettingsFailed(ErrorHandler.DEFAULT_SERVER_ERROR_MESSAGE);
                                    }
                            ));
        } else {
            userSettingsView.getUserSettingsFailed(ErrorHandler.DEFAULT_NETWORK_ERROR_MESSAGE_SHORT);
        }
    }
}