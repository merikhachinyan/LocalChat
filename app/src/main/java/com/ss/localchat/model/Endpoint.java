package com.ss.localchat.model;

import java.io.Serializable;

public class Endpoint implements Serializable{
    private String mId;
    private String mName;

    public Endpoint(String id, String name) {
        mId = id;
        mName = name;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }
}
