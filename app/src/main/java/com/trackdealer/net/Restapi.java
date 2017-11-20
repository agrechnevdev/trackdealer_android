package com.trackdealer.net;

import com.trackdealer.models.RMessage;
import com.trackdealer.models.TrackInfo;
import com.trackdealer.models.User;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Restful сервис
 * Created by grechnev-av on 20.06.2017.
 */

public interface Restapi {

    @POST("/users/register")
    Observable<Response<RMessage>> register(@Body User user);

    @GET("/users/login")
    Observable<Response<RMessage>> login(@Query("username") String username, @Query("password") String password);

    @POST("/tracks/change")
    Observable<Response<TrackInfo>> changeFavTrack(@Body TrackInfo trackInfo);

    @GET("/tracks/favtrack")
    Observable<Response<TrackInfo>> getFavTrack();

    @GET("/tracks/list")
    Observable<Response<List<TrackInfo>>> getChartTracks(@Query("lastNum") Integer page, @Query("genre") String genre);

    @GET("/tracks/like")
    Observable<Response<ResponseBody>> like(@Query("deezerId") Long deezerId, @Query("like") Boolean like);
}
