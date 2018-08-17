package com.ss.localchat.activity;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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
import com.ss.localchat.service.SendMessageService;
import com.ss.localchat.util.CircularTransformation;
import com.ss.localchat.viewmodel.MessageViewModel;

import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    public static final String USER_EXTRA = "chat.user";

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mSendMessageBinder = (SendMessageService.SendMessageBinder) service;
            mSendMessageBinder.send(mUser.getEndpointId(), mMessageText);

            unbindService(mServiceConnection);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mSendMessageBinder = null;
        }
    };


    private EditText mMessageInputEditText;

    private MessageListAdapter mMessageListAdapter;

    private SendMessageService.SendMessageBinder mSendMessageBinder;

    private MessageViewModel mMessageViewModel;

    private User mUser;

    private String mMessageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getIntent() != null) {
            mUser = (User) getIntent().getSerializableExtra(USER_EXTRA);
        }

        initActionBar();
        init();
    }

    private void initActionBar() {
        View actionBarView = LayoutInflater.from(this).inflate(R.layout.chat_activity_action_bar_custom_view, null);
        ImageView userImage = actionBarView.findViewById(R.id.user_circle_image_view_on_toolbar);
        TextView userName = actionBarView.findViewById(R.id.user_name_text_view_on_toolbar);
        TextView userInfo = actionBarView.findViewById(R.id.user_info_text_view_on_toolbar);

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
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(actionBarView);
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

        mMessageViewModel = ViewModelProviders.of(this).get(MessageViewModel.class);
        // Todo Change getMessagesWith to getReadMessagesWith
        final LiveData<List<Message>> messagesLiveData = mMessageViewModel.getMessagesWith(mUser.getId());
        messagesLiveData.observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(@Nullable List<Message> messages) {
                mMessageListAdapter.setMessages(messages);

                messagesLiveData.removeObserver(this);
            }
        });

        mMessageViewModel.getUnreadMessages(mUser.getId()).observe(this, new Observer<List<Message>>() {
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

        findViewById(R.id.send_button_chat_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMessageText = mMessageInputEditText.getText().toString().trim();
                if (!mMessageText.isEmpty()) {
                    sendMessage(mMessageText);
                    mMessageInputEditText.setText("");
                }
            }
        });
    }

    private void sendMessage(String text) {
        bindSendMessageService();

        Message message = new Message();
        message.setText(text);
        message.setRead(true);
        message.setReceiverId(mUser.getId());
        UUID userId = UUID.fromString(PreferenceManager.getDefaultSharedPreferences(this).getString("id", ""));
        message.setSenderId(userId);
        mMessageListAdapter.addMessage(message);
        mMessageViewModel.insert(message);
    }

    private void bindSendMessageService() {
        Intent intent = new Intent(this, SendMessageService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_search_in_chat:
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_clear_history_in_chat:
                Toast.makeText(this, "Clear history", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_delete_in_chat:
                Toast.makeText(this, "Delete chat", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}