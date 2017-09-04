package com.trackdealer.net;

import android.content.Context;

import com.trackdealer.models.TrackInfo;
import com.trackdealer.utils.Prefs;

import java.util.ArrayList;

import io.reactivex.Completable;
import io.reactivex.Observable;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_TRACK;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_TRACK_FAVOURITE;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_TRACK_LIST;

/**
 * Created by grechnev-av on 04.09.2017.
 */

public class FakeRestApi {

    public static Observable<ArrayList<TrackInfo>> getChartTrack(Context context) {
        ArrayList<TrackInfo> list = Prefs.getTrackList(context.getApplicationContext(), SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_LIST);
        return Observable.just(list);
    }

    public static Observable<TrackInfo> getFavouriteTrack(Context context) {
        TrackInfo trackInfo = Prefs.getTrackInfo(context.getApplicationContext(), SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_FAVOURITE);
        if(trackInfo != null)
            return Observable.just(trackInfo);
        else
            return Completable.complete().toObservable();
    }
}
