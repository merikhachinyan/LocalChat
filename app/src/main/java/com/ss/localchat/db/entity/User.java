package com.ss.localchat.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity(tableName = "users")
public class User implements Serializable {

    @PrimaryKey
    @ColumnInfo(name = "_id")
    @NonNull
    private UUID id;

    @ColumnInfo(name = "endpoint_id")
    private String endpointId;

    private String name;

    @ColumnInfo(name = "photo_url")
    private Uri photoUrl;

    @ColumnInfo(name = "registration_date")
    private Date registrationDate;

    public User() {
        this.id = UUID.randomUUID();
        registrationDate = new Date();
    }

    @NonNull
    public UUID getId() {
        return id;
    }

    public void setId(@NonNull UUID id) {
        this.id = id;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Uri getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(Uri photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = result + id.hashCode() * prime;
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

        User other = (User) obj;
        return id.equals(other.id);
    }
}
