package com.trackdealer.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.DrawableRes;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.JsonUtils;
import com.trackdealer.helpersUI.BitmapTransform;
import com.trackdealer.models.TrackInfo;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import io.reactivex.Observable;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_TRACK;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_TRACK_LIST;

/**
 * Created by grechnev-av on 29.08.2017.
 */

public class StaticUtils {

    public static ArrayList<TrackInfo> fromListTracks(List<Track> tracks) {
        ArrayList<TrackInfo> list = new ArrayList<>();
        for (Track track : tracks) {
            list.add(new TrackInfo(track.getId(), track.getTitle(), track.getArtist().getName(), track.getDuration(), track.getAlbum().getSmallImageUrl(), track.getAlbum().getId()));
        }
        return list;
    }

    public static void putDefaultTracks(Context context) {
        if (Prefs.getTrackList(context, SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_LIST) == null) {
            ArrayList<TrackInfo> tracks = new ArrayList<>();

//            User user1 = new User(1, "coolbatch", "TRACKLISTENER");
//            User user2 = new User(2, "vasilii petrovich", "TRACKLISTENER");
//            User user3 = new User(3, "YA NE VALERA!!!", "TRACKLISTENER");
//            User user4 = new User(4, "ZDAROVA", "TRACKLISTENER");
//
//            TrackInfo trackInfo3 = new TrackInfo(356306401, "Perfect Color", "SafetySuit", 234, "http://api.deezer.com/album/40882681/image", 40882681);
//            trackInfo3.setUser(user3);
//            tracks.add(trackInfo3);
//
//            TrackInfo trackInfo4 = new TrackInfo(136059064, "Numbers or Faith", "SafetySuit", 271, "http://api.deezer.com/album/14543916/image", 14543916);
//            trackInfo4.setUser(user4);
//            tracks.add(trackInfo4);
//
//            Prefs.putTrackList(context, SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_LIST, tracks);
        }
    }

    /**
     * Formats a time.
     *
     * @param time the time (in seconds)
     * @return the formatted time.
     */
    public static String formatTime(long time) {
        time /= 1000;
        long seconds = time % 60;
        time /= 60;
        long minutes = time % 60;
        time /= 60;
        long hours = time;
        StringBuilder builder = new StringBuilder(8);
        doubleDigit(builder, seconds);
        builder.insert(0, ':');
        if (hours == 0) {
            builder.insert(0, minutes);
        } else {
            doubleDigit(builder, minutes);
            builder.insert(0, ':');
            builder.insert(0, hours);
        }
        return builder.toString();
    }

    /**
     * Ensure double decimal representation of numbers.
     *
     * @param builder a builder where a number is gonna be inserted at beginning.
     * @param value   the number value. If below 10 then a leading 0 is inserted.
     */
    public static void doubleDigit(final StringBuilder builder, final long value) {
        builder.insert(0, value);
        if (value < 10) {
            builder.insert(0, '0');
        }
    }

    public static Bitmap cutPicture(Context context, @DrawableRes int draw) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int screenHeight = metrics.heightPixels;
        int screenWidth = metrics.widthPixels;
        int resourceStatus = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = context.getResources().getDimensionPixelSize(resourceStatus);

        BitmapTransform bitmapTransform = new BitmapTransform(screenWidth, screenHeight - statusBarHeight, 0, 0);
        Bitmap background = BitmapFactory.decodeResource(context.getResources(), draw);
        return bitmapTransform.transform(background);
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static Observable<Object> requestFromDeezer(DeezerConnect deezerConnect, DeezerRequest deezerRequest) {

        return Observable.create(e -> {
            String requestList2;
            requestList2 = deezerConnect.requestSync(deezerRequest);
            Object var3 = null;
            Object var4;
            if ((var4 = (new JSONTokener(requestList2)).nextValue()) instanceof JSONObject) {
                var3 = JsonUtils.deserializeObject((JSONObject) var4);
            } else if (var4 instanceof JSONArray) {
                var3 = JsonUtils.deserializeArray((JSONArray) var4);
            }
            if (var3 != null)
                e.onNext(var3);
            else
                e.onNext(new Object());

        });
    }

    public static UUID generateUUID(Context context, String userName, String pass) {
        UUID uuid = null;
        try {
            uuid = UUID.nameUUIDFromBytes((userName + pass).getBytes("utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return uuid;
    }

    public static String dateFormat(String oldDateString) {
        try {
            DateFormat oldFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
            Date oldDate = oldFormat.parse(oldDateString);
            DateFormat newFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            return newFormat.format(oldDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }
}
