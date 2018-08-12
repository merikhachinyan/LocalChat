package com.ss.localchat.model;

import android.net.Uri;

public class User {

    private String mName;
    private String mProfilePhotoUrl;
    private String mMessage = "";

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

    public void setName(String name) {
        mName = name;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        mProfilePhotoUrl = profilePhotoUrl;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }
}
