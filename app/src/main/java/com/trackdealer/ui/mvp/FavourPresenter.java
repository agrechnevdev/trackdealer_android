package com.trackdealer.ui.mvp;

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
 * Created by grechnev-av on 27.09.2017.
 */

public class FavourPresenter extends BasePresenter<FavourView> {

    private final String TAG = "FavourPresenter ";

    private CompositeDisposable subscription;
    private FavourView favourView;
    private final Restapi restapi;
    private Context context;

    public FavourPresenter(Restapi restapi, Context context) {
        subscription = new CompositeDisposable();
        this.context = context;
        this.restapi = restapi;
    }

    @Override
    public void attachView(FavourView favourView) {
        this.favourView = favourView;
    }

    @Override
    public void detachView() {
        favourView = null;
        subscription.dispose();
    }

    public void loadFavourTrack() {
        if (ConnectionsManager.isOnline(context)) {
            subscription.add(
                    restapi.getFavTrack()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    response -> {
                                        Timber.e(TAG + " loadFavourTrack response code: " + response.code());
                                        if (response.isSuccessful()) {
                                            favourView.loadFavourTrackSuccess(response.body());
                                        } else {
                                            favourView.loadFavourTrackFailed(ErrorHandler.getErrorMessageFromResponse(context, response));
                                        }
                                    },
                                    ex -> {
                                        Timber.e(ex, TAG + " loadFavourTrack onError() " + ex.getMessage());
                                        favourView.loadFavourTrackFailed(ErrorHandler.buildErrorDescriptionShort(context, ex));
                                    }
                            ));
        } else {
            favourView.loadFavourTrackFailed(context.getString(R.string.default_network_error));
        }
    }

}
