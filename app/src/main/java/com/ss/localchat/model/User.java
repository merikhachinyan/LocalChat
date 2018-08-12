package com.ss.localchat.model;

public class User {

    private String mName;
    private String mProfilePhotoUrl;

    public User(String name, String profilePhotoUrl) {
        mName = name;
        mProfilePhotoUrl = profilePhotoUrl;
    }

    public String getName() {
        return mName;
    }

    public String getProfilePhotoUrl() {
        return mProfilePhotoUrl;
    }
}
