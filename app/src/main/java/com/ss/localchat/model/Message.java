package com.ss.localchat.model;

import java.util.Date;

public class Message {

    private String text;
    private User sender;
    private Date date;

    public Message(String text, User sender) {
        this.text = text;
        this.sender = sender;
        date = new Date();
    }

    public String getText() {
        return text;
    }

    public User getSender() {
        return sender;
    }

    public Date getDate() {
        return date;
    }
}
