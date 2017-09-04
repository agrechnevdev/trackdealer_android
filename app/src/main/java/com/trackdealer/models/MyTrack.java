package com.trackdealer.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by grechnev-av on 30.08.2017.
 */

public class MyTrack{

    @SerializedName("id")
    String id;
    @SerializedName("title")
    String title;
    @SerializedName("duration")
    String duration;
    @SerializedName("preview")
    String preview;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }
}
