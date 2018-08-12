package com.ss.localchat.model;

import java.util.Date;

public class Message {

    private String mText;
    private User mSender;
    private Date mDate;

    public Message(String text, User sender) {
        mText = text;
        mSender = sender;
        mDate = new Date();
    }

    public String getText() {
        return mText;
    }

    public User getSender() {
        return mSender;
    }

    public Date getDate() {
        return mDate;
    }
}
