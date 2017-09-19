package com.trackdealer.helpersUI;

import com.deezer.sdk.model.Track;
import com.trackdealer.models.PositionPlay;
import com.trackdealer.models.TrackInfo;

import java.util.List;

/**
 * Created by grechnev-av on 19.09.2017.
 */

public class PlayingSingleton {

    private final String TAG = "PlayingSingleton";
    private static PlayingSingleton instance = null;

    public PositionPlay positionPlay;
    public Track playingTrack;
    public List<TrackInfo> trackList;

    private PlayingSingleton() {
    }

    public static synchronized PlayingSingleton getInstance() {
        if (instance == null)
            instance = new PlayingSingleton();
        return instance;
    }


}
