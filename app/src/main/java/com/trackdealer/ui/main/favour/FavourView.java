package com.trackdealer.ui.main.favour;

import com.trackdealer.base.BaseView;
import com.trackdealer.models.TrackInfo;

/**
 * Created by grechnev-av on 27.09.2017.
 */

public interface FavourView  extends BaseView{

    void loadFavourTrackSuccess(TrackInfo trackInfo);

    void loadFavourTrackFailed(String error);


}
