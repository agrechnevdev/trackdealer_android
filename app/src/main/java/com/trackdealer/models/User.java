package com.trackdealer.models;

/**
 * Created by grechnev-av on 12.09.2017.
 */

public class User {

    private Integer id;
    private String username;
    private String status;

    public User(Integer id, String username, String status) {
        this.id = id;
        this.username = username;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
