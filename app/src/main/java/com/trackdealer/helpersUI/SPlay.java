package com.trackdealer.helpersUI;

import com.deezer.sdk.model.Track;
import com.trackdealer.models.PositionPlay;
import com.trackdealer.models.TrackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by grechnev-av on 19.09.2017.
 */

public class SPlay {

    private final String TAG = "SPlay";
    private static SPlay instance = null;

    public PositionPlay positionPlay;
    public Track playingTrack;
    public List<TrackInfo> playList = new ArrayList<>();
    public List<TrackInfo> showList = new ArrayList<>();

    public boolean favSongs = false;
    private SPlay() {
    }

    public static synchronized SPlay init() {
        if (instance == null)
            instance = new SPlay();
        return instance;
    }

    public Long getPlayingTrackId() {
        if(playingTrack != null)
            return playingTrack.getId();
        else
            return null;
    }
}
