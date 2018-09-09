package com.ss.localchat.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.ss.localchat.db.MessageRepository;
import com.ss.localchat.db.entity.Message;

import java.util.List;
import java.util.UUID;

public class MessageViewModel extends AndroidViewModel {

    private MessageRepository messageRepository;


    public MessageViewModel(@NonNull Application application) {
        super(application);
        messageRepository = new MessageRepository(application);
    }

    public LiveData<List<Message>> getMessagesWith(UUID id) {
        return messageRepository.getMessagesWith(id);
    }

    public LiveData<List<Message>> getReadOrUnreadMessagesWith(UUID id, boolean isRead, boolean isGroup) {
        return messageRepository.getReadOrUnreadMessagesWith(id, isRead, isGroup);
    }

    public LiveData<List<Message>> getReceiverUnreadMessages(UUID user_id, boolean is_read_receiver) {
        return messageRepository.getReceiverUnreadMessages(user_id, is_read_receiver);
    }

    public void update(Message... messages) {
        messageRepository.update(messages);
    }

    public void insert(Message message) {
        messageRepository.insert(message);
    }

    public void delete(Message message) {
        messageRepository.delete(message);
    }

    public void clearHistory(UUID id) {
        messageRepository.clearHistory(id);
    }
}
