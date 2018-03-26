package com.trackdealer.ui.main.profile;

import android.content.Context;

import com.trackdealer.R;
import com.trackdealer.base.BasePresenter;
import com.trackdealer.net.Restapi;
import com.trackdealer.utils.ConnectionsManager;
import com.trackdealer.utils.ErrorHandler;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by grechnev-av on 22.11.2017.
 */

public class ProfilePresenter extends BasePresenter<ProfileView> {

    private final String TAG = "ProfilePresenter ";

    private CompositeDisposable subscription;
    private ProfileView profileView;
    private final Restapi restapi;
    private Context context;

    ProfilePresenter(Restapi restapi, Context context) {
        subscription = new CompositeDisposable();
        this.context = context;
        this.restapi = restapi;
    }

    @Override
    public void attachView(ProfileView profileView) {
        this.profileView = profileView;
    }

    @Override
    public void detachView() {
        profileView = null;
        subscription.dispose();
    }

    void logout() {
        if (ConnectionsManager.isOnline(context)) {
            subscription.add(
                    restapi.logout()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    response -> {
                                        Timber.e(TAG + " logout response code: " + response.code());
                                        if (response.isSuccessful()) {
                                            profileView.logoutSuccess();
                                        } else {
                                            profileView.logoutFailed(ErrorHandler.getErrorMessageFromResponse(context, response));
                                        }
                                    },
                                    ex -> {
                                        Timber.e(ex, TAG + " logout onError() " + ex.getMessage());
                                        profileView.logoutFailed(ErrorHandler.buildErrorDescriptionShort(context, ex));
                                    }
                            ));
        } else {
            profileView.logoutFailed(context.getString(R.string.default_network_error));
        }
    }

}
