package com.ss.localchat.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.ss.localchat.db.GroupRepository;
import com.ss.localchat.db.entity.Group;
import com.ss.localchat.db.entity.GroupChat;

import java.util.List;
import java.util.UUID;

public class GroupViewModel extends AndroidViewModel {

    private GroupRepository groupRepository;


    public GroupViewModel(@NonNull Application application) {
        super(application);
        groupRepository = new GroupRepository(application);
    }

    public LiveData<List<GroupChat>> getGroups() {
        return groupRepository.getGroups();
    }

    public LiveData<Group> getGroupById(UUID id) {
        return groupRepository.getGroupById(id);
    }

    public void insert(Group group) {
        groupRepository.insert(group);
    }

    public void update(Group group) {
        groupRepository.update(group);
    }

    public void delete(Group group) {
        groupRepository.delete(group);
    }
    public void deleteAllGroups() {
        groupRepository.deleteAllGrouops();
    }
}
