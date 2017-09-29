package com.trackdealer.utils;

import android.content.Context;

import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.JsonUtils;
import com.trackdealer.models.TrackInfo;
import com.trackdealer.models.User;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_TRACK;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_TRACK_LIST;

/**
 * Created by grechnev-av on 29.08.2017.
 */

public class StaticUtils {

    public static ArrayList<TrackInfo> fromListTracks(List<Track> tracks){
        ArrayList<TrackInfo> list = new ArrayList<>();
        for(Track track : tracks){
            Long id = track.getId();
            list.add(new TrackInfo(id.intValue(), track.getTitle(), track.getArtist().getName(), track.getDuration(), track.getAlbum().getSmallImageUrl()));
        }
        return list;
    }

    public static void putDefaultTracks(Context context) {
        if (Prefs.getTrackList(context, SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_LIST) == null) {
            ArrayList<TrackInfo> tracks = new ArrayList<>();

            User user1 = new User(1, "coolbatch");
            User user2 = new User(2, "vasilii petrovich");
            User user3 = new User(3, "YA NE VALERA!!!");
            User user4 = new User(4, "ZDAROVA");

            TrackInfo trackInfo1 = new TrackInfo(15911969, "Staring At It", "SafetySuit", 257, "http://api.deezer.com/album/1472647/image");
            trackInfo1.setUser(user1);
            tracks.add(trackInfo1);

            TrackInfo trackInfo2 = new TrackInfo(15911968, "Let Go", "SafetySuit", 200, "http://api.deezer.com/album/1472647/image");
            trackInfo2.setUser(user2);
            tracks.add(trackInfo2);

            TrackInfo trackInfo3 = new TrackInfo(356306401, "Perfect Color", "SafetySuit", 234, "http://api.deezer.com/album/40882681/image");
            trackInfo3.setUser(user3);
            tracks.add(trackInfo3);

            TrackInfo trackInfo4 = new TrackInfo(136059064, "Numbers or Faith", "SafetySuit", 271, "http://api.deezer.com/album/14543916/image");
            trackInfo4.setUser(user4);
            tracks.add(trackInfo4);

            Prefs.putTrackList(context, SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_LIST, tracks);
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
            e.onNext(var3);
        });
    }
}
