package com.trackdealer.net;

import android.content.Context;

import com.trackdealer.models.TrackInfo;
import com.trackdealer.utils.Prefs;

import java.util.ArrayList;
import java.util.List;

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

    public static Observable<Response<List<TrackInfo>>> getChartTrack(Context context) {
        List<TrackInfo> list = Prefs.getTrackList(context.getApplicationContext(), SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_LIST);
        return Observable.just(Response.success(list));
    }

    public static Observable<Response<TrackInfo>> getFavouriteTrack(Context context) {
        TrackInfo trackInfo = Prefs.getTrackInfo(context.getApplicationContext(), SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_FAVOURITE);
        if (trackInfo != null)
            return Observable.just(Response.success(trackInfo));
        else
            return Completable.complete().toObservable();
    }

    public static Observable<Response<ResponseBody>> login(Context context) {
        return Observable.just(Response.success(ResponseBody.create(MediaType.parse("application/json"), "")));
//        return Observable.just(Response.error(500, ResponseBody.create(MediaType.parse("application/json"), "Здарова это ошибка")));
    }

    public static Observable<Response<ResponseBody>> trackLike(Context context, long trackInfoId, Boolean like) {
        ArrayList<TrackInfo> list = Prefs.getTrackList(context.getApplicationContext(), SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_LIST);
        for(TrackInfo trackInfo : list){
            if(trackInfo.getTrackId() == trackInfoId){
                if(like)
                    trackInfo.setLikes(trackInfo.getLikes() + 1);
                else
                    trackInfo.setDislikes(trackInfo.getDislikes() + 1);
                trackInfo.setUserLike(like);
                Prefs.putTrackList(context, SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_LIST, list);
                break;
            }
        }
        return  Observable.just(Response.success(ResponseBody.create(MediaType.parse("application/json"), "")));
    }
}
