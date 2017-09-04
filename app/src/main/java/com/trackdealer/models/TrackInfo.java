package com.trackdealer.models;

import java.util.Random;

/**
 * Created by grechnev-av on 29.08.2017.
 */

public class TrackInfo {

    private Integer trackId;
    private String title;
    private String artist;
    private Integer duration;

    private Integer likes;
    private Integer dislikes;

    public TrackInfo(Integer trackId, String title, String artist, Integer duration) {
        this.trackId = trackId;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        likes = new Random().nextInt(10000);
        dislikes = new Random().nextInt(10000);
    }

    public Integer getTrackId() {
        return trackId;
    }

    public void setTrackId(Integer trackId) {
        this.trackId = trackId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getDuration() {
        Integer min = duration / 60;
        Integer sec = duration % 60;
        String minStr = min.toString();
        String secStr = null;
        if(sec < 10) {
            secStr = "0"+sec.toString();
        } else {
            secStr = sec.toString();
        }
        return  minStr + ":" + secStr;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Integer getDislikes() {
        return dislikes;
    }

    public void setDislikes(Integer dislikes) {
        this.dislikes = dislikes;
    }
}
