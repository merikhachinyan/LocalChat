package com.ss.localchat.model;

import java.io.Serializable;

public class User implements Serializable {

    private String name;
    private String profilePhotoUrl;
    private String message = "Last Message";

    public User(String name, String profilePhotoUrl) {
        this.name = name;
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
