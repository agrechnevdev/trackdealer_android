package com.trackdealer.helpersUI;

import com.deezer.sdk.model.Track;
import com.trackdealer.models.TrackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by grechnev-av on 19.09.2017.
 */

public class SPlay {

    private final String TAG = "SPlay";
    private static SPlay instance = null;

    public Long playTrackId;
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

    public Integer getPosPlayForPlayList(Long trackId) {
        for (int i = 0; i < playList.size(); i++) {
            if (trackId != null && trackId == playList.get(i).getTrackId()) {
                return i;
            }
        }
        return -1;
    }

    public Integer getPosPlayForIndicator(Long trackId) {
        for (int i = 0; i < showList.size(); i++) {
            if (trackId != null && trackId == showList.get(i).getTrackId()) {
                return i;
            }
        }
        return -1;
    }
}
