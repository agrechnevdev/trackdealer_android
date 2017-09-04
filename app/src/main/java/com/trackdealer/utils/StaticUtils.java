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
            list.add(new TrackInfo(id.intValue(), track.getTitle(), track.getArtist().getName(), track.getDuration()));
        }
        return list;
    }

    public static void putDefaultTracks(Context context) {
        if (Prefs.getTrackList(context, SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_LIST) == null) {
            ArrayList<TrackInfo> tracks = new ArrayList<>();
            tracks.add(new TrackInfo(15911969, "Staring At It", "SafetySuit", 257));
            tracks.add(new TrackInfo(15911968, "Let Go", "SafetySuit", 200));
            tracks.add(new TrackInfo(356306401, "Perfect Color", "SafetySuit", 234));
            tracks.add(new TrackInfo(136059064, "Numbers or Faith", "SafetySuit", 271));
            Prefs.putTrackList(context, SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_LIST, tracks);
        }
    }
}
