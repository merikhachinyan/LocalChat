package com.ss.localchat.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReadMessageViewModel extends ViewModel {

//    private MutableLiveData<List<UUID>> mReadMessages = new MutableLiveData<>();

    private MutableLiveData<UUID> mData = new MutableLiveData<>();

    private List<UUID> mUsers = new ArrayList<>();

    public void setReadMessages(UUID uuid) {
//        mUsers.add(uuid);
//        mReadMessages.setValue(mUsers);
        mData.setValue(uuid);
    }

//    public MutableLiveData<List<UUID>> getReadMessages() {
//        return mReadMessages;
//    }


    public MutableLiveData<UUID> getData() {
        return mData;
    }
}
