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

    @Query("SELECT * FROM users WHERE _id != :owner")
    LiveData<List<User>> getUsersExceptOwner(UUID owner);

    @Query("SELECT users.*, messages.*, count FROM users, messages, " +
            "(SELECT users._id, sum(case messages.is_read when 0 then 1 else 0 end) as count FROM users, messages " +
            "WHERE users._id in (messages.sender_id, messages.receiver_id) AND messages.is_group = 0 " +
            "GROUP BY users._id) as unread_count " +
            "WHERE users._id in (messages.sender_id, messages.receiver_id) AND users._id != :owner AND " +
            "messages.is_group = 0 AND users._id = unread_count._id " +
            "GROUP BY users._id " +
            "HAVING MAX(messages.date) " +
            "ORDER BY messages.date DESC")
    LiveData<List<Chat>> getChatsExceptOwner(UUID owner);


    @Query("SELECT * FROM users WHERE _id = :id LIMIT 1")
    LiveData<User> getUserById(UUID id);

    @Query("SELECT * FROM users WHERE _id in (:uuids)")
    LiveData<List<User>> getUsersListById(UUID... uuids);


    @Query("SELECT endpoint_id FROM users WHERE _id in (:uuids)")
    LiveData<List<String>> getEndpointId(UUID... uuids);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(User user);

    @Query("UPDATE users SET photo_url = :photoUri WHERE endpoint_id = :endpointId")
    void updatePhoto(String endpointId, String photoUri);

    @Query("DELETE FROM users WHERE _id = :id")
    void delete(UUID id);

    @Query("DELETE FROM users" )
    void deleteAllUsers();
}
