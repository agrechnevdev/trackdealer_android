package com.trackdealer.net;

import com.trackdealer.models.TrackInfo;
import com.trackdealer.models.User;
import com.trackdealer.models.UserSettings;

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
    Observable<Response<ResponseBody>> register(@Body User user);

    @GET("/users/login")
    Observable<Response<ResponseBody>> login(@Query("username") String username, @Query("password") String password);

    @GET("/users/settings")
    Observable<Response<UserSettings>> getUserSettings(@Query("version") String version);

    @GET("/users/logout")
    Observable<Response<ResponseBody>> logout();

    @POST("/tracks/change")
    Observable<Response<TrackInfo>> changeFavTrack(@Body TrackInfo trackInfo);

    @GET("/tracks/favtrack")
    Observable<Response<TrackInfo>> getFavTrack();

    @GET("/tracks/list")
    Observable<Response<List<TrackInfo>>> getChartTracks(@Query("lastNum") Integer lastNum, @Query("genre") String genre);

//    @GET("/tracks/lastperiodlist")
//    Observable<Response<List<TrackInfo>>> getLastPeriodTracks();

    @GET("/tracks/periodsList")
    Observable<Response<List<TrackInfo>>> getPeriodsTracks(@Query("date") String date);

    @GET("/tracks/randomlist")
    Observable<Response<List<TrackInfo>>> randomList(@Query("genre") String genre);

    @GET("/tracks/userlist")
    Observable<Response<List<TrackInfo>>> userList(@Query("username") String username);

    @GET("/tracks/like")
    Observable<Response<ResponseBody>> like(@Query("deezerId") Long deezerId, @Query("like") Boolean like);

    @GET("/tracks/updatelike")
    Observable<Response<ResponseBody>> updateLike(@Query("deezerId") Long deezerId, @Query("like") Boolean like);

    @GET("/tracks/deletelike")
    Observable<Response<ResponseBody>> deleteLike(@Query("deezerId") Long deezerId, @Query("like") Boolean like);

    @GET("/users/promo")
    Observable<Response<ResponseBody>> promo(@Query("promo") String promo);
}
