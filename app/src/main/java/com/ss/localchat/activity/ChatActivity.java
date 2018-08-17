package com.ss.localchat.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.ss.localchat.R;
import com.ss.localchat.adapter.MessageListAdapter;
import com.ss.localchat.model.Message;
import com.ss.localchat.model.User;
import com.ss.localchat.service.SendMessageService;
import com.ss.localchat.util.CircularTransformation;

public class ChatActivity extends AppCompatActivity {

    public static final String USER_EXTRA = "chat.user";
    public static final String NEW_USER_EXTRA = "new.user";

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mSendMessageBinder = (SendMessageService.SendMessageBinder)service;
            mSendMessageBinder.send(mUserId, mMessageText);

            unbindService(mServiceConnection);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mSendMessageBinder = null;
        }
    };

    public static boolean isCurrentlyRunning;

    private EditText mMessageInputEditText;

    private MessageListAdapter mMessageListAdapter;

    private SendMessageService.SendMessageBinder mSendMessageBinder;

    private User mUser;

    private String mMessageText;
    public String mUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getIntent() != null) {
             mUser = (User) getIntent().getSerializableExtra(NEW_USER_EXTRA);
             if(mUser != null){
                 mUserId = mUser.getId();

                 Toast.makeText(this, "New User", Toast.LENGTH_SHORT).show();
             } else {
                 mUser = (User) getIntent().getSerializableExtra(USER_EXTRA);
             }
        }

        initActionBar();
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();

        isCurrentlyRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();

        isCurrentlyRunning = false;
    }

    private void initActionBar() {
        View actionBarView = LayoutInflater.from(this).inflate(R.layout.chat_activity_action_bar_custom_view, null);
        ImageView userImage = actionBarView.findViewById(R.id.user_circle_image_view_on_toolbar);
        TextView userName = actionBarView.findViewById(R.id.user_name_text_view_on_toolbar);
        TextView userInfo = actionBarView.findViewById(R.id.user_info_text_view_on_toolbar);

        userName.setText(mUser.getName());
        if (mUser.getProfilePhotoUrl() == null) {
            Picasso.get()
                    .load(R.drawable.no_user_image)
                    .transform(new CircularTransformation())
                    .into(userImage);
        } else {
            Picasso.get()
                    .load(mUser.getProfilePhotoUrl())
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
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mMessageInputEditText = findViewById(R.id.message_input_edit_text_chat_activity);
        mMessageInputEditText.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        mMessageInputEditText.requestFocus();
        final RecyclerView recyclerView = findViewById(R.id.recycler_view_chat_activity);

        mMessageListAdapter = new MessageListAdapter();
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

        ImageButton sendImageButton = findViewById(R.id.send_button_chat_activity);

        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMessageText = mMessageInputEditText.getText().toString().trim();
                if (!mMessageText.isEmpty()) {
                    bindSendMessageService();

                    sendMessage(mMessageText);
                    mMessageInputEditText.setText("");
                }
            }
        });
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
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendMessage(String text) {
        mMessageListAdapter.addMessage(new Message(text, null));
    }

    private void bindSendMessageService(){
        Intent intent = new Intent(this, SendMessageService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }
}