package com.ss.localchat.activity;

import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.ss.localchat.R;
import com.ss.localchat.adapter.NewGroupUsersListAdapter;
import com.ss.localchat.db.entity.Group;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.preferences.Preferences;
import com.ss.localchat.service.ChatService;
import com.ss.localchat.util.DividerItemDecoration;
import com.ss.localchat.util.Util;
import com.ss.localchat.viewmodel.GroupViewModel;
import com.ss.localchat.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.UUID;

public class NewGroupActivity extends AppCompatActivity {

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceBinder = (ChatService.ServiceBinder) service;
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBinder = null;
            isBound = false;
        }
    };

    private ChatService.ServiceBinder mServiceBinder;

    private boolean isBound;

    private NewGroupUsersListAdapter mNewGroupUsersListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        bindService(new Intent(this, ChatService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        init();
    }

    private void init() {
        mNewGroupUsersListAdapter = new NewGroupUsersListAdapter();

        UUID myUserId = Preferences.getUserId(getApplicationContext());

        UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        userViewModel.getUsersExceptOwner(myUserId).observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(@Nullable List<User> users) {
                mNewGroupUsersListAdapter.setUsers(users);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view_new_group_activity);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mNewGroupUsersListAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, Util.dpToPx(this, 80)));
    }

    private void showSetGroupNameDialog(final Set<User> checkedUsers) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set group name");

        View view = LayoutInflater.from(this).inflate(R.layout.set_group_name_dialog_view, null);
        final EditText inputName = view.findViewById(R.id.group_name_edit_text);
        inputName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        inputName.setHint("Name");

        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = inputName.getText().toString().trim();
                if (groupName.length() == 0)
                    return;

                String groupMembers = Preferences.getUserId(getApplicationContext()).toString().concat(";");
                Iterator<User> iterator = checkedUsers.iterator();

                List<UUID> uuidList = new ArrayList<>();
                while (iterator.hasNext()) {
                    String id = iterator.next().getId().toString();
                    uuidList.add(UUID.fromString(id));
                    groupMembers = groupMembers.concat(id);
                    if (iterator.hasNext()) {
                        groupMembers = groupMembers.concat(";");
                    }
                }

                final Group group = new Group();
                group.setName(groupName);
                group.setMembers(groupMembers);

                GroupViewModel groupViewModel = ViewModelProviders.of(NewGroupActivity.this).get(GroupViewModel.class);
                groupViewModel.insert(group);

                final UUID[] uuidListArray = uuidList.toArray(new UUID[uuidList.size()]);
                final UserViewModel userViewModel = ViewModelProviders.of(NewGroupActivity.this).get(UserViewModel.class);
                userViewModel.getEndpointId(uuidListArray).observe(NewGroupActivity.this, new Observer<List<String>>() {
                    @Override
                    public void onChanged(@Nullable List<String> strings) {
                        if (strings != null) {
                            if (isBound) {
                                mServiceBinder.sendGroupTo(strings, group);
                            }
                        }
                        userViewModel.getEndpointId(uuidListArray).removeObserver(this);
                    }
                });

                setResult(RESULT_OK);
                finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_group_activity, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_create_new_group:
                Set<User> checkedUsers = mNewGroupUsersListAdapter.getCheckedUsers();

                if (checkedUsers.size() > 0) {
                    showSetGroupNameDialog(checkedUsers);
                }

                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
