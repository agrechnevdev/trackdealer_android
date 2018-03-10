package com.trackdealer.helpersUI;

import com.trackdealer.R;

/**
 * Created by grechnev-av on 23.11.2017.
 */

public enum PlaylistType {

    MAIN (true, R.string.main_chart_text),
    DEEZER(false, R.string.deezer_chart_text),
    RANDOM(true, R.string.random_chart_text),
    FINISHED(false, R.string.finished_chart_text),
    USER(true, R.string.user_chart_text);

    public final boolean showLikes;
    public final int title;

    PlaylistType(boolean showLikes, int title) {
        this.showLikes = showLikes;
        this.title = title;
    }

    public int getTitle() {
        return title;
    }
}
