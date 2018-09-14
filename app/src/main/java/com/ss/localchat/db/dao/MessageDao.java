package com.ss.localchat.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.ss.localchat.db.entity.Message;

import java.util.List;
import java.util.UUID;

@Dao
public interface MessageDao {

    @Query("SELECT * FROM messages " +
            "WHERE :id in (sender_id, receiver_id) " +
            "ORDER BY date ASC")
    LiveData<List<Message>> getMessagesWith(UUID id);

    @Query("SELECT * FROM messages " +
            "WHERE :id in (sender_id, receiver_id) AND is_read = :isRead AND  is_group = :isGroup " +
            "ORDER BY date ASC")
    LiveData<List<Message>> getReadOrUnreadMessagesWith(UUID id, boolean isRead, boolean isGroup);

    @Query("SELECT * FROM messages " +
            "WHERE receiver_id = :user_id AND is_read_receiver = :is_read_receiver " +
            "ORDER BY date ASC")
    LiveData<List<Message>> getReceiverUnreadMessages(UUID user_id, boolean is_read_receiver);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Message... messages);

    @Insert
    void insert(Message message);

    @Delete
    void delete(Message message);

    @Query("DELETE FROM messages " +
            "WHERE :id in (sender_id, receiver_id)")
    void clearHistory(UUID id);

    @Query("DELETE FROM messages" )
    void deleteAllMessages();


}
