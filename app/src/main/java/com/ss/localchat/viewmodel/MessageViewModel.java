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

    public LiveData<List<Message>> getAllMessages() {
        return messageRepository.getAllMessages();
    }

    public LiveData<List<Message>> getMessagesWith(UUID user_id) {
        return messageRepository.getMessagesWith(user_id);
    }

    public LiveData<Message> getLastMessage(UUID user_id) {
        return messageRepository.getLastMessage(user_id);
    }

    public LiveData<List<Message>> getReadOrUnreadMessagesWith(UUID user_id, boolean is_read) {
        return messageRepository.getReadOrUnreadMessagesWith(user_id, is_read);
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

    public void clearHistory(UUID user_id) {
        messageRepository.clearHistory(user_id);
    }
}
