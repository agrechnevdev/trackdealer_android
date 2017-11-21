package com.trackdealer.ui.main.chart;

import com.trackdealer.base.BaseView;
import com.trackdealer.models.TrackInfo;

import java.util.List;

/**
 * Created by grechnev-av on 27.09.2017.
 */

public interface ChartView extends BaseView{

    void loadTrackListSuccess(int offset, List<TrackInfo> list);

    void loadTrackListFailed(String error);

    void loadTrackListFailed(Exception ex, int lastNum);

    void trackLikeSuccess();

    void trackLikeFailed(String error);

}
