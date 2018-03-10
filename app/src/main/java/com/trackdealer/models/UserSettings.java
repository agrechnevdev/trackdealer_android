package com.trackdealer.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by anton on 05.01.2018.
 */

public class UserSettings {

    @SerializedName("username")
    private String username;

    @SerializedName("name")
    private String name;

    @SerializedName("status")
    private String status;

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }
}
