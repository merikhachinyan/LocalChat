package com.ss.localchat.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.ss.localchat.db.converter.DateTypeConverter;
import com.ss.localchat.db.converter.UUIDTypeConverter;
import com.ss.localchat.db.converter.UriTypeConverter;
import com.ss.localchat.db.dao.GroupDao;
import com.ss.localchat.db.dao.MessageDao;
import com.ss.localchat.db.dao.UserDao;
import com.ss.localchat.db.entity.Group;
import com.ss.localchat.db.entity.Message;
import com.ss.localchat.db.entity.User;

@Database(entities = {User.class, Message.class, Group.class}, version = 1)
@TypeConverters(value = {DateTypeConverter.class, UUIDTypeConverter.class, UriTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "localchat.db";

    private static AppDatabase sInstance;


    public abstract UserDao userDao();

    public abstract MessageDao messageDao();

    public abstract GroupDao groupDao();


    public static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (AppDatabase.class) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME)
                        .build();
            }
        }
        return sInstance;
    }
}
