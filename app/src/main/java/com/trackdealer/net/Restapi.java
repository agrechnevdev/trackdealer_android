package com.trackdealer.net;

import com.trackdealer.models.MyTrack;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Restful сервис
 * Created by grechnev-av on 20.06.2017.
 */

public interface Restapi {

    @GET()
    Observable<Response<MyTrack>> getTrackById(@Url String url);

}
