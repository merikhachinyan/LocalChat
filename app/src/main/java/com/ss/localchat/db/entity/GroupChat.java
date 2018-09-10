package com.ss.localchat.db.entity;

import android.arch.persistence.room.Embedded;

public class GroupChat {

    @Embedded
    public Group group;

    @Embedded
    public Message message;

    public int count;
}
