package com.trackdealer.models;

import com.deezer.sdk.model.Genre;

import java.util.Random;

/**
 * Created by grechnev-av on 29.08.2017.
 */

public class TrackInfo {

    private long trackId;
    private String title;
    private String artist;
    private Integer duration;
    private String coverImage;

    private Integer likes;
    private Integer dislikes;

    private Boolean userLike;

    private User user;
    private long albumId;
    private Genre genre;

    public TrackInfo(long trackId, String title, String artist, Integer duration, String coverImage, long albumId) {
        this.trackId = trackId;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.coverImage = coverImage;
        likes = new Random().nextInt(10000);
        dislikes = new Random().nextInt(10000);
        this.albumId = albumId;
    }

    public long getTrackId() {
        return trackId;
    }

    public void setTrackId(long trackId) {
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

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getUserLike() {
        return userLike;
    }

    public void setUserLike(Boolean userLike) {
        this.userLike = userLike;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }
}
