package com.ss.localchat.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.ss.localchat.db.UserRepository;
import com.ss.localchat.db.entity.User;

import java.util.List;
import java.util.UUID;

public class UserViewModel extends AndroidViewModel {

    private UserRepository userRepository;


    public UserViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public LiveData<List<User>> getUsersExceptOwner(UUID owner) {
        return userRepository.getUsersExceptOwner(owner);
    }

    public LiveData<User> getUserById(UUID id) {
        return userRepository.getUserById(id);
    }

    public LiveData<User> getUserByEndpointId(String endpointId) {
        return userRepository.getUserByEndpointId(endpointId);
    }

    public void insert(User user) {
        userRepository.insert(user);
    }

    public void update(User user) {
        userRepository.update(user);
    }

    public void delete(UUID id) {
        userRepository.delete(id);
    }
}
