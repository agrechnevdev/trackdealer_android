package com.trackdealer.ui.main.chart;

import android.content.Context;

import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.trackdealer.base.BasePresenter;
import com.trackdealer.models.TrackInfo;
import com.trackdealer.net.Restapi;
import com.trackdealer.utils.ConnectionsManager;
import com.trackdealer.utils.ErrorHandler;
import com.trackdealer.utils.StaticUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by grechnev-av on 27.09.2017.
 */

public class ChartPresenter extends BasePresenter<ChartView> {

    private final String TAG = "ChartPresenter ";

    private CompositeDisposable subscription;
    private ChartView chartView;
    private final Restapi restapi;
    private Context context;

    ChartPresenter(Restapi restapi, Context context) {
        subscription = new CompositeDisposable();
        this.context = context;
        this.restapi = restapi;
    }

    @Override
    public void attachView(ChartView chartView) {
        this.chartView = chartView;
    }

    @Override
    public void detachView() {
        chartView = null;
        subscription.dispose();
    }

    void loadTrackList(Integer lastNum, String genre) {
        if (ConnectionsManager.isOnline(context)) {
            subscription.add(
                    restapi.getChartTracks(lastNum, genre)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    response -> {
                                        Timber.e(TAG + " loadTrackList response code: " + response.code());
                                        if (response.isSuccessful()) {
                                            List<TrackInfo> list = response.body();
                                            if (response.body() == null)
                                                list = new ArrayList<>();
                                            chartView.loadTrackListSuccess(lastNum, list);
                                        } else {
                                            chartView.loadTrackListFailed(ErrorHandler.getErrorMessageFromResponse(response));
                                        }
                                    },
                                    ex -> {
                                        Timber.e(ex, TAG + " loadTrackList onError() " + ex.getMessage());
                                        chartView.loadTrackListFailed(ErrorHandler.buildErrorDescriptionShort(ex));
                                    }
                            ));
        } else {
            chartView.loadTrackListFailed(ErrorHandler.DEFAULT_NETWORK_ERROR_MESSAGE_SHORT);
        }
    }

    public void loadFavSongs(Integer index, DeezerConnect mDeezerConnect) {
        if (ConnectionsManager.isOnline(context)) {
            DeezerRequest request = DeezerRequestFactory.requestCurrentUserTracks();
            request.addParam("access_token", mDeezerConnect.getAccessToken());
            request.addParam("index", index.toString());
            subscription.add(StaticUtils.requestFromDeezer(mDeezerConnect, request)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(obj -> {
                                chartView.loadTrackListSuccess(index, StaticUtils.fromListTracks((List<Track>) obj));
                            },
                            ex -> {
                                Timber.e(ex, TAG + " loadFavSongs onError() " + ex.getMessage());
                                chartView.loadTrackListFailed((Exception) ex, index);
                            }
                    ));
        } else {
            chartView.loadTrackListFailed(ErrorHandler.DEFAULT_NETWORK_ERROR_MESSAGE_SHORT);
        }
    }

    void trackLike(long trackId, Boolean like) {
        if (ConnectionsManager.isOnline(context)) {
            subscription.add(
                    restapi.like(trackId, like)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    response -> {
                                        Timber.e(TAG + " trackLike response code: " + response.code());
                                        if (response.isSuccessful()) {
                                            chartView.trackLikeSuccess();
                                        } else {
                                            chartView.trackLikeFailed(ErrorHandler.getErrorMessageFromResponse(response));
                                        }
                                    },
                                    ex -> {
                                        Timber.e(ex, TAG + " trackLike onError() " + ex.getMessage());
                                        chartView.trackLikeFailed(ErrorHandler.buildErrorDescriptionShort(ex));
                                    }
                            ));
        } else {
            chartView.trackLikeFailed(ErrorHandler.DEFAULT_NETWORK_ERROR_MESSAGE_SHORT);
        }
    }
}
