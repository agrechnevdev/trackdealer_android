package com.trackdealer.net;

import android.content.Context;

import com.trackdealer.models.TrackInfo;
import com.trackdealer.utils.Prefs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_TRACK;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_TRACK_FAVOURITE;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_TRACK_LIST;

/**
 * Created by grechnev-av on 04.09.2017.
 */

public class FakeRestApi {

    public static Observable<Response<List<TrackInfo>>> getChartTrack(Context context, String genre) {
        List<TrackInfo> list = Prefs.getTrackList(context.getApplicationContext(), SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_LIST);
        List<TrackInfo> listFiltered = new ArrayList<>();
        if (genre.equals("Все"))
            return Observable.just(Response.success(list)).delay(2, TimeUnit.SECONDS);
        else {
            for (TrackInfo trackInfo : list) {
                if (trackInfo.getGenre() != null && trackInfo.getGenre().getName().equals(genre)) {
                    listFiltered.add(trackInfo);
                }
            }
            return Observable.just(Response.success(listFiltered)).delay(2, TimeUnit.SECONDS);
        }
    }

    public static Observable<Response<TrackInfo>> getFavouriteTrack(Context context) {
        TrackInfo trackInfo = Prefs.getTrackInfo(context.getApplicationContext(), SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_FAVOURITE);
        if (trackInfo != null)
            return Observable.just(Response.success(trackInfo)).delay(2, TimeUnit.SECONDS);
        else
            return Completable.complete().toObservable();
    }

    public static Observable<Response<ResponseBody>> login(Context context, String login, String password) {
        if(login.contains("1")) {
            ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "");
            Response<ResponseBody> response = Response.success(responseBody);
            return Observable.just(response).delay(2, TimeUnit.SECONDS);
        }
        else {
            ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "Логин должен содержать цифру 1");
            Response<ResponseBody> response = Response.error(responseBody, new okhttp3.Response.Builder() //
                    .code(600)
                    .protocol(Protocol.HTTP_1_1)
                    .request(new Request.Builder().url("http://localhost/").build())
                    .build());
            return Observable.just(response);
        }
    }

    public static Observable<Response<ResponseBody>> trackLike(Context context, long trackInfoId, Boolean like) {
        ArrayList<TrackInfo> list = Prefs.getTrackList(context.getApplicationContext(), SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_LIST);
        for (TrackInfo trackInfo : list) {
            if (trackInfo.getTrackId() == trackInfoId) {
                if (like)
                    trackInfo.setLikes(trackInfo.getLikes() + 1);
                else
                    trackInfo.setDislikes(trackInfo.getDislikes() + 1);
                trackInfo.setUserLike(like);
                Prefs.putTrackList(context, SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_LIST, list);
                break;
            }
        }
        return Observable.just(Response.success(ResponseBody.create(MediaType.parse("application/json"), ""))).delay(2, TimeUnit.SECONDS);
    }
}
