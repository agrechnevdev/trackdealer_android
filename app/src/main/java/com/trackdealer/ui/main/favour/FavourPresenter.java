package com.trackdealer.ui.main.favour;

import android.content.Context;

import com.trackdealer.base.BasePresenter;
import com.trackdealer.net.FakeRestApi;
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

    FavourPresenter(Restapi restapi, Context context) {
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

    void loadFavourTrack(){
        if (ConnectionsManager.isOnline(context)) {
            subscription.add(FakeRestApi.getFavouriteTrack(context)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            response -> {
                                Timber.e(TAG + " loadFavourTrack response code: " + response.code());
                                if (response.isSuccessful()) {
                                    favourView.loadFavourTrackSuccess(response.body());
                                } else {
                                    favourView.loadFavourTrackFailed(ErrorHandler.getErrorMessageFromResponse(response));
                                }
                            },
                            ex -> {
                                Timber.e(ex, TAG + " loadFavourTrack onError() " + ex.getMessage());
                                favourView.loadFavourTrackFailed(ErrorHandler.buildErrorDescriptionShort(ex));
                            },
                            () -> {
                                favourView.loadFavourTrackFailed("Требуется загрузить песню");
                            }
                    ));
        } else {
            favourView.loadFavourTrackFailed(ErrorHandler.DEFAULT_NETWORK_ERROR_MESSAGE_SHORT);
        }
    }

}
