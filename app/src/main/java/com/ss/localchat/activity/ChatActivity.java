package com.ss.localchat.activity;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableArrayMap;
import android.databinding.ObservableList;
import android.databinding.ObservableMap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.squareup.picasso.Picasso;
import com.ss.localchat.R;
import com.ss.localchat.adapter.MessageListAdapter;
import com.ss.localchat.db.entity.Message;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.helper.NotificationHelper;
import com.ss.localchat.model.ConnectionState;
import com.ss.localchat.preferences.Preferences;
import com.ss.localchat.service.ChatService;
import com.ss.localchat.util.CircularTransformation;
import com.ss.localchat.viewmodel.MessageViewModel;
import com.ss.localchat.viewmodel.ReadMessageViewModel;
import com.ss.localchat.viewmodel.UserViewModel;

import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    public static final String USER_EXTRA = "chat.user";

    private static final String CONNECTING = "Connecting";

    private static final String CONNECTED = "Connected";

    private static final String DISCONNECTED = "Disconnected";

    public static final String READ_MESSAGE = "00read00";


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mSendMessageBinder = (ChatService.ServiceBinder) service;

            mSendMessageBinder.setOnMapChangedListener(mListener);

            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mSendMessageBinder = null;

            isBound = false;
        }
    };

    private ChatService.OnMapChangedListener mListener = new ChatService.OnMapChangedListener() {
        @Override
        public void onMapChanged(ObservableMap<String, ConnectionState> map) {
            setUserInfo((ObservableArrayMap<String, ConnectionState>) map);
        }
    };

    public static boolean isCurrentlyRunning;

    public static UUID currentUserId;

    private EditText mMessageInputEditText;

    private TextView mUserInfo;

    private MessageListAdapter mMessageListAdapter;

    private ChatService.ServiceBinder mSendMessageBinder;

    private MessageViewModel mMessageViewModel;

    private UserViewModel mUserViewModel;

    private User mUser;

    private ConnectionState mState;

    private String mMessageText;

    private boolean isBound;

    private ObservableArrayMap<String, ConnectionState> endpoints;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
        }

        if (getIntent() != null) {
            mUser = (User) getIntent().getSerializableExtra(USER_EXTRA);

            currentUserId = mUser.getId();
        }

        bindService(new Intent(this, ChatService.class), mServiceConnection, Context.BIND_AUTO_CREATE);

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();

        cancelNotification();

        isCurrentlyRunning = true;
    }


    @Override
    protected void onStop() {
        super.onStop();

        isCurrentlyRunning = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isBound) {
            unbindService(mServiceConnection);
        }
    }

    private void initActionBar() {
        View actionBarView = LayoutInflater.from(this).inflate(R.layout.chat_activity_action_bar_custom_view, null);

        ImageView userImage = actionBarView.findViewById(R.id.user_circle_image_view_on_toolbar);

        TextView userName = actionBarView.findViewById(R.id.user_name_text_view_on_toolbar);

        mUserInfo = actionBarView.findViewById(R.id.user_info_text_view_on_toolbar);


        userName.setText(mUser.getName());

        if (mUser.getPhotoUrl() == null) {
            Picasso.get()
                    .load(R.drawable.no_user_image)
                    .transform(new CircularTransformation())
                    .into(userImage);
        } else {
            Picasso.get()
                    .load(mUser.getPhotoUrl())
                    .transform(new CircularTransformation())
                    .into(userImage);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setCustomView(actionBarView);
        }

        endpoints = mSendMessageBinder.getEndpoints();

        setUserInfo(endpoints);

        if (endpoints != null) {
            if (endpoints.containsKey(mUser.getEndpointId()) && endpoints.get(mUser.getEndpointId()).equals(ConnectionState.CONNECTED)) {
                mSendMessageBinder.markMessageAsRead(mUser.getEndpointId(), READ_MESSAGE);
            }
        }
    }

    private void init() {

        mMessageInputEditText = findViewById(R.id.message_input_edit_text_chat_activity);
        mMessageInputEditText.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        mMessageInputEditText.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);



        final RecyclerView recyclerView = findViewById(R.id.recycler_view_chat_activity);

        mMessageListAdapter = new MessageListAdapter(this);
        mMessageListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.smoothScrollToPosition(mMessageListAdapter.getItemCount());
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mMessageListAdapter);

        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mUserViewModel.getUserById(mUser.getId()).observe(this, new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                if (user == null) {
                    return;
                }
                mUser = user;
                initActionBar();
            }
        });


        mMessageViewModel = ViewModelProviders.of(this).get(MessageViewModel.class);

        final LiveData<List<Message>> messagesLiveData = mMessageViewModel.getReadOrUnreadMessagesWith(mUser.getId(), true);
        messagesLiveData.observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(@Nullable List<Message> messages) {
                mMessageListAdapter.addMessages(messages);
                messagesLiveData.removeObserver(this);

                mMessageViewModel.getReadOrUnreadMessagesWith(mUser.getId(), false).observe(ChatActivity.this, new Observer<List<Message>>() {
                    @Override
                    public void onChanged(@Nullable List<Message> messages) {
                        if (messages == null || messages.size() == 0)
                            return;

                        for (Message message : messages) {
                            message.setRead(true);
                        }
                        mMessageViewModel.update(messages.toArray(new Message[messages.size()]));
                        mMessageListAdapter.addMessages(messages);
                    }
                });
            }
        });


        findViewById(R.id.send_button_chat_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMessageText = mMessageInputEditText.getText().toString().trim();
                if (mState == ConnectionState.DISCONNECTED) {
                    Snackbar.make(v, DISCONNECTED + " from " + mUser.getName(), Snackbar.LENGTH_LONG).show();
                } else {
                    if (!mMessageText.isEmpty()) {
                        if (isBound) {
                            mSendMessageBinder.sendMessageTo(mUser.getEndpointId(), mMessageText);
                        }
                        sendMessage(mMessageText);
                        mMessageInputEditText.setText("");
                    }
                }
            }
        });

        mMessageViewModel.getUnreadMessagesWith(mUser.getId(), false).observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(@Nullable List<Message> messages) {
                if (messages.size() == 0) {
                    mMessageListAdapter.setReceiverMessageIsRead(mUser.getId());
                }
            }
        });
    }

    private void sendMessage(String text) {
        UUID myUserId = Preferences.getUserId(getApplicationContext());

        Message message = new Message();
        message.setText(text);
        message.setRead(false);
        message.setReceiverId(mUser.getId());
        message.setSenderId(myUserId);
        mMessageViewModel.insert(message);
    }

    private void setUserInfo(ObservableArrayMap<String, ConnectionState> endpoints) {
        if (endpoints.containsKey(mUser.getEndpointId())) {
            mState = endpoints.get(mUser.getEndpointId());

            //TODO change mUserInfo.setText();
            switch (mState) {
                case CONNECTING:
                    mUserInfo.setText(CONNECTING);
                    break;
                case CONNECTED:
                    mUserInfo.setText(CONNECTED);
                    break;
                case DISCONNECTED:
                    mUserInfo.setText(DISCONNECTED);
                    break;
            }
        } else {
            mState = ConnectionState.DISCONNECTED;
            mUserInfo.setText(DISCONNECTED);
        }
    }


    private void cancelNotification() {
        NotificationHelper.getManager(this).cancel(mUser.getId().toString(), NotificationHelper.MESSAGE_NOTIFICATION_ID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear_history_in_chat:
                mMessageViewModel.clearHistory(mUser.getId());
                mMessageListAdapter.clear();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}