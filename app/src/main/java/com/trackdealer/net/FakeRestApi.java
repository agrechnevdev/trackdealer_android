package com.trackdealer.net;

import android.content.Context;

import com.trackdealer.models.TrackInfo;
import com.trackdealer.utils.Prefs;

import java.util.ArrayList;

import io.reactivex.Completable;
import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

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
        if (trackInfo != null)
            return Observable.just(trackInfo);
        else
            return Completable.complete().toObservable();
    }

    public static Observable<Response<ResponseBody>> login(Context context) {
        return Observable.just(Response.success(ResponseBody.create(MediaType.parse("application/json"), "")));
//        return Observable.just(Response.error(500, ResponseBody.create(MediaType.parse("application/json"), "Здарова это ошибка")));
    }
}
