package com.ss.localchat.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.ss.localchat.db.dao.MessageDao;
import com.ss.localchat.db.entity.Message;

import java.util.List;
import java.util.UUID;

public class MessageRepository {

    private MessageDao messageDao;


    public MessageRepository(Application application) {
        messageDao = AppDatabase.getInstance(application).messageDao();
    }

    public LiveData<List<Message>> getAllMessages() {
        return messageDao.getAllMessages();
    }

    public LiveData<List<Message>> getMessagesWith(UUID user_id) {
        return messageDao.getMessagesWith(user_id);
    }

    public LiveData<Message> getLastMessage(UUID user_id) {
        return messageDao.getLastMessage(user_id);
    }

    public LiveData<List<Message>> getReadOrUnreadMessagesWith(UUID user_id, boolean is_read) {
        return messageDao.getReadOrUnreadMessagesWith(user_id, is_read);
    }



    public void update(Message... messages) {
        new UpdateAsyncTask(messageDao).execute(messages);
    }

    public void insert(Message message) {
        new InsertAsyncTask(messageDao).execute(message);
    }

    public void delete(Message message) {
        new DeleteAsyncTask(messageDao).execute(message);
    }

    public void clearHistory(final UUID user_id) {
        new ClearHistoryAsyncTask(messageDao).execute(user_id);
    }

    private static class UpdateAsyncTask extends AsyncTask<Message, Void, Void> {

        private MessageDao asyncTaskDao;

        UpdateAsyncTask(MessageDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            asyncTaskDao.update(messages);
            return null;
        }
    }

    private static class InsertAsyncTask extends AsyncTask<Message, Void, Void> {

        private MessageDao asyncTaskDao;

        InsertAsyncTask(MessageDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            asyncTaskDao.insert(messages[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Message, Void, Void> {

        private MessageDao asyncTaskDao;

        DeleteAsyncTask(MessageDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            asyncTaskDao.delete(messages[0]);
            return null;
        }
    }

    private static class ClearHistoryAsyncTask extends AsyncTask<UUID, Void, Void> {

        private MessageDao asyncTaskDao;

        ClearHistoryAsyncTask(MessageDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(UUID... uuids) {
            asyncTaskDao.clearHistory(uuids[0]);
            return null;
        }
    }

}
