package com.trackdealer.interfaces;

/**
 * Created by grechnev-av on 27.09.2017.
 */

public interface ITrackOperation {

    void trackLike(long trackId, Boolean like);

    void updateLike(long trackId, Boolean like);

    void delteLike(long trackId, Boolean like);

    void clickUser(String username);
}
