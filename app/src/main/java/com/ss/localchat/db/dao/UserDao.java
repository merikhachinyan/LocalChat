package com.ss.localchat.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.ss.localchat.db.entity.User;

import java.util.List;
import java.util.UUID;

@Dao
public interface UserDao {

    @Query("SELECT * FROM users WHERE _id != :owner")
    LiveData<List<User>> getUsersExceptOwner(UUID owner);

    @Query("SELECT * FROM users WHERE _id = :id")
    LiveData<User> getUserById(UUID id);

    @Query("SELECT * FROM users WHERE endpoint_id = :endpointId")
    LiveData<User> getUserByEndpointId(String endpointId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(User user);

    @Query("DELETE FROM users WHERE _id = :id")
    void delete(UUID id);
}
