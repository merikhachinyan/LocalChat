package com.ss.localchat.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.ss.localchat.db.entity.User;
import com.ss.localchat.db.entity.Chat;

import java.util.List;
import java.util.UUID;

@Dao
public interface UserDao {

//    @Query("SELECT users.*, last_message.*, count FROM users " +
//            "LEFT JOIN " +
//            "(SELECT * FROM messages " +
//            "ORDER BY messages.date DESC LIMIT 1) last_message ON users._id in (last_message.sender_id, last_message.receiver_id) " +
//            "LEFT JOIN " +
//            "(SELECT messages.sender_id, messages.receiver_id, COUNT(*) AS count FROM messages " +
//            "WHERE messages.is_read = 0) unread_count ON users._id in (unread_count.sender_id, unread_count.receiver_id) " +
//            "WHERE users._id != :owner " +
//            "ORDER BY last_message.date DESC")
//    LiveData<List<Chat>> getUsersExceptOwner(UUID owner);


    @Query("SELECT users.*, messages.*, count FROM users, messages " +
            "LEFT JOIN " +
            "(SELECT messages.sender_id, messages.receiver_id, COUNT(*) AS count FROM messages " +
            "WHERE messages.is_read = 0) unread_count ON users._id in (unread_count.sender_id, unread_count.receiver_id) " +
            "WHERE users._id in (messages.sender_id, messages.receiver_id) AND users._id != :owner " +
            "GROUP BY users._id " +
            "HAVING MAX(messages.date) " +
            "ORDER BY messages.date DESC")
    LiveData<List<Chat>> getUsersExceptOwner(UUID owner);


    @Query("SELECT * FROM users WHERE _id = :id LIMIT 1")
    LiveData<User> getUserById(UUID id);

    @Query("SELECT * FROM users WHERE endpoint_id = :endpointId LIMIT 1")
    LiveData<User> getUserByEndpointId(String endpointId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(User user);

    @Query("UPDATE users SET photo_url = :photoUri WHERE endpoint_id = :endpointId")
    void updatePhoto(String endpointId, String photoUri);

    @Query("DELETE FROM users WHERE _id = :id")
    void delete(UUID id);
}
