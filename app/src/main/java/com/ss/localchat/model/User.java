package com.ss.localchat.model;

import java.io.Serializable;

public class User implements Serializable {

    private String id;
    private String name;
    private String profilePhotoUrl;

    public User(String id, String name, String profilePhotoUrl) {
        this.id = id;
        this.name = name;
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
