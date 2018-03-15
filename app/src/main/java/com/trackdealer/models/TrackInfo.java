package com.trackdealer.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by grechnev-av on 29.08.2017.
 */

public class TrackInfo {

    @SerializedName("id")
    private Long id;
    @SerializedName("deezerId")
    private Long deezerId;
    @SerializedName("title")
    private String title;
    @SerializedName("artist")
    private String artist;
    @SerializedName("duration")
    private Integer duration;
    @SerializedName("genre")
    private String genre;
    @SerializedName("coverImage")
    private String coverImage;
    @SerializedName("finished")
    private Boolean finished;
    @SerializedName("countLike")
    private Long countLike;
    @SerializedName("countDislike")
    private Long countDislike;
    @SerializedName("finishDate")
    private String finishDate;
    @SerializedName("first")
    private Boolean first;

    @SerializedName("userLike")
    private Boolean userLike;
    @SerializedName("userNameLoad")
    private String userNameLoad;

    private long albumId;

    public TrackInfo() {
    }

    public TrackInfo(Long deezerId, String title, String artist, Integer duration, String coverImage, long albumId) {
        this.deezerId = deezerId;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.coverImage = coverImage;
//        likes = new Random().nextInt(10000);
//        dislikes = new Random().nextInt(10000);
        countLike = 0L;
        countDislike = 0L;
        this.albumId = albumId;
    }

    public Long getDeezerId() {
        return deezerId;
    }

    public void setDeezerId(long deezerId) {
        this.deezerId = deezerId;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public Long getCountLike() {
        return countLike;
    }

    public void setCountLike(Long countLike) {
        this.countLike = countLike;
    }

    public Long getCountDislike() {
        return countDislike;
    }

    public void setCountDislike(Long countDislike) {
        this.countDislike = countDislike;
    }

    public Boolean getUserLike() {
        return userLike;
    }

    public void setUserLike(Boolean userLike) {
        this.userLike = userLike;
    }

    public String getUserNameLoad() {
        return userNameLoad;
    }

    public void setUserNameLoad(String userNameLoad) {
        this.userNameLoad = userNameLoad;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public void setDeezerId(Long deezerId) {
        this.deezerId = deezerId;
    }

    public String getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(String finishDate) {
        this.finishDate = finishDate;
    }

    public Boolean getFirst() {
        return first;
    }

    public void setFirst(Boolean first) {
        this.first = first;
    }
}
