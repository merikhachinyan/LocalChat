package com.ss.localchat.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.ss.localchat.db.dao.UserDao;
import com.ss.localchat.db.entity.Chat;
import com.ss.localchat.db.entity.User;

import java.util.List;
import java.util.UUID;

public class UserRepository {

    private UserDao userDao;


    public UserRepository(Application application) {
        userDao = AppDatabase.getInstance(application).userDao();
    }

    public LiveData<List<Chat>> getUsersExceptOwner(UUID owner) {
        return userDao.getUsersExceptOwner(owner);
    }

    public LiveData<User> getUserById(UUID id) {
        return userDao.getUserById(id);
    }

    public LiveData<User> getUserByEndpointId(String endpointId) {
        return userDao.getUserByEndpointId(endpointId);
    }

    public void insert(User user) {
        new InsertAsyncTask(userDao).execute(user);
    }

    public void update(User user) {
        new UpdateAsyncTask(userDao).execute(user);
    }

    public void delete(UUID id) {
        new DeleteAsyncTask(userDao).execute(id);
    }

    private static class InsertAsyncTask extends AsyncTask<User, Void, Void> {

        private UserDao asyncTaskDao;

        InsertAsyncTask(UserDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(User... users) {
            asyncTaskDao.insert(users[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<User, Void, Void> {

        private UserDao asyncTaskDao;

        UpdateAsyncTask(UserDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(User... users) {
            asyncTaskDao.update(users[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<UUID, Void, Void> {

        private UserDao asyncTaskDao;

        DeleteAsyncTask(UserDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(UUID... uuids) {
            asyncTaskDao.delete(uuids[0]);
            return null;
        }
    }
}
