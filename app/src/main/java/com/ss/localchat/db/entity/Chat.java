package com.ss.localchat.db.entity;

import android.arch.persistence.room.Embedded;

public class Chat {

    @Embedded
    public User user;

    @Embedded
    public Message message;

    public int count;
}
