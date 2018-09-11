package com.ss.localchat.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.ss.localchat.db.entity.Group;
import com.ss.localchat.db.entity.GroupChat;

import java.util.List;
import java.util.UUID;

@Dao
public interface GroupDao {

    @Query("SELECT groups.*, messages.*, count FROM groups\n" +
            "LEFT JOIN \n" +
            "(SELECT messages.* FROM messages ORDER BY messages.date DESC) messages \n" +
            "ON groups._id in (messages.sender_id, messages.receiver_id) AND messages.is_group = 1 \n" +
            "LEFT JOIN \n" +
            "(SELECT groups._id, sum(case messages.is_read when 0 then 1 else 0 end) as count FROM groups, messages \n" +
            "WHERE groups._id in (messages.sender_id, messages.receiver_id) AND messages.is_group = 1\n" +
            "GROUP BY groups._id) unread_count ON groups._id = unread_count._id\n" +
            "GROUP BY groups._id \n" +
            "ORDER BY messages.date DESC")
    LiveData<List<GroupChat>> getGroups();

    @Query("SELECT * FROM groups WHERE _id = :id LIMIT 1")
    LiveData<Group> getGroupById(UUID id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Group group);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Group group);

    @Delete
    void delete(Group group);

    @Query("DELETE FROM groups" )
    void deleteAllGroups();
}
