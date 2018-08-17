package com.ss.localchat.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.UUID;

@Entity(tableName = "messages")
//        foreignKeys = @ForeignKey(entity = User.class, parentColumns = {"_id", "_id"}, childColumns = {"sender_id", "receiver_id"}))
public class Message {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    @NonNull
    private Long id;

    @ColumnInfo(name = "sender_id")
    private UUID senderId;

    @ColumnInfo(name = "receiver_id")
    private UUID receiverId;

    private String text;

    private String photo;

    private Date date;

    @ColumnInfo(name = "is_read")
    private boolean isRead;

    public Message() {
        date = new Date();
    }

    @NonNull
    public Long getId() {
        return id;
    }

    public void setId(@NonNull Long id) {
        this.id = id;
    }

    public UUID getSenderId() {
        return senderId;
    }


    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public UUID getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(UUID receiverId) {
        this.receiverId = receiverId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = result + ((id == null) ? 0 : id.hashCode()) * prime;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;

        Message other = (Message) obj;
        return id == null ? other.id == null : id.equals(other.id);
    }
}
