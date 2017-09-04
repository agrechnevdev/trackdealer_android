package com.trackdealer.utils;

import android.content.Context;

import com.deezer.sdk.model.Track;
import com.trackdealer.models.TrackInfo;

import java.util.ArrayList;
import java.util.List;

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
            tracks.add(new TrackInfo(15911969, "Staring At It", "SafetySuit", 257, "http://api.deezer.com/album/1472647/image"));
            tracks.add(new TrackInfo(15911968, "Let Go", "SafetySuit", 200, "http://api.deezer.com/album/1472647/image"));
            tracks.add(new TrackInfo(356306401, "Perfect Color", "SafetySuit", 234, "http://api.deezer.com/album/40882681/image"));
            tracks.add(new TrackInfo(136059064, "Numbers or Faith", "SafetySuit", 271, "http://api.deezer.com/album/14543916/image"));
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
}
