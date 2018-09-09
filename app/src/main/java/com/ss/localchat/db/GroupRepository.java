package com.ss.localchat.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.ss.localchat.db.dao.GroupDao;
import com.ss.localchat.db.entity.Group;
import com.ss.localchat.db.entity.GroupChat;

import java.util.List;
import java.util.UUID;

public class GroupRepository {

    private GroupDao groupDao;

    public GroupRepository(Application application) {
        groupDao = AppDatabase.getInstance(application.getApplicationContext()).groupDao();
    }

    public LiveData<List<GroupChat>> getGroups() {
        return groupDao.getGroups();
    }

    public LiveData<Group> getGroupById(UUID id) {
        return groupDao.getGroupById(id);
    }

    public void insert(Group group) {
        new InsertAsyncTask(groupDao).execute(group);
    }

    public void update(Group group) {
        new UpdateAsyncTask(groupDao).execute(group);
    }

    public void delete(Group group) {
        new DeleteAsyncTask(groupDao).execute(group);
    }

    private static class InsertAsyncTask extends AsyncTask<Group, Void, Void> {

        private GroupDao asyncTaskDao;

        InsertAsyncTask(GroupDao groupDao) {
            asyncTaskDao = groupDao;
        }

        @Override
        protected Void doInBackground(Group... groups) {
            asyncTaskDao.insert(groups[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Group, Void, Void> {

        private GroupDao asyncTaskDao;

        UpdateAsyncTask(GroupDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Group... groups) {
            asyncTaskDao.update(groups[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Group, Void, Void> {

        private GroupDao asyncTaskDao;

        DeleteAsyncTask(GroupDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Group... groups) {
            asyncTaskDao.delete(groups[0]);
            return null;
        }
    }
}
