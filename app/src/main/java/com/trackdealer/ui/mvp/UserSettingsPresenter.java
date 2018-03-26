package com.trackdealer.ui.mvp;

import android.content.Context;

import com.trackdealer.BuildConfig;
import com.trackdealer.R;
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
                    restapi.getUserSettings(BuildConfig.VERSION_NAME)
                            .delay(delay, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(response -> {
                                        Timber.e(TAG + " register response code: " + response.code());
                                        if (response.isSuccessful()) {
                                            userSettingsView.getUserSettingsSuccess(response.body());
                                        } else {
                                            userSettingsView.getUserSettingsFailed(ErrorHandler.getErrorMessageFromResponse(context, response));
                                        }
                                    },
                                    ex -> {
                                        Timber.e(ex, TAG + " register onError() " + ex.getMessage());
                                        userSettingsView.getUserSettingsFailed(ErrorHandler.buildErrorDescriptionShort(context, ex));
                                    }
                            ));
        } else {
            userSettingsView.getUserSettingsFailed(context.getString(R.string.default_network_error));
        }
    }
}