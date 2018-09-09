package com.ss.localchat.activity;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.ObservableArrayMap;
import android.databinding.ObservableMap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.rockerhieu.emojicon.EmojiconEditText;
import com.ss.localchat.R;
import com.ss.localchat.adapter.MessageListAdapter;
import com.ss.localchat.db.entity.Message;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.fragment.ShowPhotoFragment;
import com.ss.localchat.helper.NotificationHelper;
import com.ss.localchat.model.ConnectionState;
import com.ss.localchat.preferences.Preferences;
import com.ss.localchat.service.ChatService;
import com.ss.localchat.view.EmojiKeyboardLayout;
import com.ss.localchat.viewmodel.MessageViewModel;
import com.ss.localchat.viewmodel.UserViewModel;

import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    public static final String FRAGMENT_TAG_EMOJI = "emoji";

    public static final String FRAGMENT_TAG_KEYBOARD = "custom_keyboard";

    public static final String USER_EXTRA = "chat.user";

    private static final String CONNECTING_INFO = "Connecting";

    private static final String CONNECTED_INFO = "Connected";

    private static final String DISCONNECTED_INFO = "Disconnected";

    public static final String READ_MESSAGE = "read";

    public static final String FRAGMENT_TAG = "show.photo";

    public static final int REQUEST_CODE_CHOOSE_PICTURE = 2;


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mSendMessageBinder = (ChatService.ServiceBinder) service;
            mSendMessageBinder.setOnMapChangedListener(mListener);
            isBound = true;
            initActionBar();
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

    private MessageListAdapter.OnImageClickListener mImageClickListener = new MessageListAdapter.OnImageClickListener() {
        @Override
        public void OnImageClick(Message message) {
            showPhoto(message.getPhoto());
        }
    };

    public static boolean isCurrentlyRunning;

    public static UUID currentUserId;

    private EmojiconEditText mMessageInputEditText;

    private TextView mUserInfo;

    private ImageView mUserImage;

    private TextView mUserName;

    private ImageView mChosenPhotoImage;

    private ImageView mRemovePhotoImage;

    private MessageListAdapter mMessageListAdapter;

    private ChatService.ServiceBinder mSendMessageBinder;

    private MessageViewModel mMessageViewModel;

    private User mUser;

    private ConnectionState mState;

    private String mMessageText;

    private boolean isBound;

    private ObservableArrayMap<String, ConnectionState> mEndpoints;

    private Uri mPhotoUri;

    private View mView;


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

            View actionBarView = LayoutInflater.from(this).inflate(R.layout.chat_activity_action_bar_custom_view, null);
            mUserImage = actionBarView.findViewById(R.id.user_circle_image_view_on_toolbar);
            mUserName = actionBarView.findViewById(R.id.user_name_text_view_on_toolbar);
            mUserInfo = actionBarView.findViewById(R.id.user_info_text_view_on_toolbar);
            actionBar.setCustomView(actionBarView);

        }

        mUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String photoUri = mUser.getPhotoUrl();
                if (photoUri != null) {
                    showPhoto(mUser.getPhotoUrl());
                }
            }
        });

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
        mUserName.setText(mUser.getName());

        Glide.with(this)
                .load(mUser.getPhotoUrl())
                .apply(RequestOptions.circleCropTransform().diskCacheStrategy(DiskCacheStrategy.ALL))
                .error(Glide.with(this).load(R.drawable.no_user_image).apply(RequestOptions.circleCropTransform()))
                .into(mUserImage);

        mEndpoints = mSendMessageBinder.getEndpoints();
        setUserInfo(mEndpoints);

        if (mEndpoints != null) {
            if (mEndpoints.containsKey(mUser.getEndpointId()) && mEndpoints.get(mUser.getEndpointId()).equals(ConnectionState.CONNECTED)) {
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

        mChosenPhotoImage = findViewById(R.id.attach_photo_image_view);
        mRemovePhotoImage = findViewById(R.id.remove_attached_photo_image_view);
        mRemovePhotoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhotoUri = null;
                mChosenPhotoImage.setVisibility(View.GONE);
                mRemovePhotoImage.setVisibility(View.GONE);
                setLayoutParams(1);
            }
        });

        final RecyclerView recyclerView = findViewById(R.id.recycler_view_chat_activity);

        mMessageListAdapter = new MessageListAdapter(this);
        mMessageListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.smoothScrollToPosition(mMessageListAdapter.getItemCount());
            }
        });
        mMessageListAdapter.setOnImageClickListener(mImageClickListener);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mMessageListAdapter);
        EmojiKeyboardLayout keyboardLayout = findViewById(R.id.keyboardLayout);
        keyboardLayout.setup(this, recyclerView);

        UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        userViewModel.getUserById(mUser.getId()).observe(this, new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                if (user != null) {
                    mUser = user;
                    initActionBar();
                }
            }
        });


        mMessageViewModel = ViewModelProviders.of(this).get(MessageViewModel.class);

        final LiveData<List<Message>> messagesLiveData = mMessageViewModel.getReadOrUnreadMessagesWith(mUser.getId(), true, false);
        messagesLiveData.observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(@Nullable List<Message> messages) {
                mMessageListAdapter.addMessages(messages);
                messagesLiveData.removeObserver(this);

                mMessageViewModel.getReadOrUnreadMessagesWith(mUser.getId(), false, false).observe(ChatActivity.this, new Observer<List<Message>>() {
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
                String messageText = mMessageInputEditText.getText().toString().trim();
                if (mState == ConnectionState.DISCONNECTED) {
                    Snackbar.make(v, DISCONNECTED_INFO + " from " + mUser.getName(), Snackbar.LENGTH_LONG).show();
                } else {
                    if (!mMessageText.isEmpty() || mPhotoUri != null) {
                        if (isBound) {
                            if (mPhotoUri == null) {
                                mSendMessageBinder.sendMessageTo(mUser.getEndpointId(), mMessageText);
                                sendMessage(mMessageText, null);
                            }
                            else {
                                if (mMessageText.isEmpty()){
                                    mSendMessageBinder.sendPhotoMessage(mUser.getEndpointId(), mPhotoUri);
                                    sendMessage(null, mPhotoUri.toString());
                                } else {
                                    mSendMessageBinder.sendPhotoWithTextMessage(mUser.getEndpointId(), mPhotoUri, mMessageText);
                                    sendMessage(mMessageText, mPhotoUri.toString());
                                }
                                mChosenPhotoImage.setVisibility(View.GONE);
                                mRemovePhotoImage.setVisibility(View.GONE);
                                setLayoutParams(1);
                            }
                        }
                        mMessageInputEditText.setText("");
                        mPhotoUri = null;
                    }
                }
            }
        });

        findViewById(R.id.attach_photo_chat_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhoto();
            }
        });

        mMessageViewModel.getReceiverUnreadMessages(mUser.getId(), false).observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(@Nullable List<Message> messages) {
                if (messages != null) {
                    if (messages.size() == 0) {
                        mMessageListAdapter.setReceiverMessageIsRead(mUser.getId());
                    }
                }
            }
        });
    }

    private void sendMessage(String text, String photoUrl) {
    private void sendMessage(String text) {
        if (isBound) {
            mSendMessageBinder.sendMessageTo(mUser.getEndpointId(), text);
        }

        UUID myUserId = Preferences.getUserId(getApplicationContext());
        String myUserName = Preferences.getUserName(getApplicationContext());

        Message message = new Message();
        message.setText(text);
        message.setRead(false);
        message.setReceiverId(mUser.getId());
        message.setSenderId(myUserId);
        message.setGroup(false);
        message.setSenderName(myUserName);
        message.setPhoto(photoUrl);
        mMessageViewModel.insert(message);
    }

    private void setUserInfo(ObservableArrayMap<String, ConnectionState> endpoints) {
        if (endpoints.containsKey(mUser.getEndpointId())) {
            mState = endpoints.get(mUser.getEndpointId());
            invalidateOptionsMenu();

            switch (mState) {
                case CONNECTING:
                    mUserInfo.setText(CONNECTING_INFO);
                    break;
                case CONNECTED:
                    mUserInfo.setText(CONNECTED_INFO);
                    break;
                case DISCONNECTED:
                    mUserInfo.setText(DISCONNECTED_INFO);
                    break;
            }
        } else {
            mState = ConnectionState.DISCONNECTED;
            mUserInfo.setText(DISCONNECTED_INFO);
        }
    }

    private void cancelNotification() {
        NotificationHelper.getManager(this).cancel(mUser.getId().toString(), NotificationHelper.MESSAGE_NOTIFICATION_ID);
    }

    private void choosePhoto() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_CHOOSE_PICTURE);
    }

    private void showPhoto(String photoUri) {
        ShowPhotoFragment fragment = ShowPhotoFragment.newInstance(photoUri);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, FRAGMENT_TAG)
                .addToBackStack(FRAGMENT_TAG)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CHOOSE_PICTURE) {
            if (data != null) {
                mPhotoUri = data.getData();

                mChosenPhotoImage.setVisibility(View.VISIBLE);
                mRemovePhotoImage.setVisibility(View.VISIBLE);

                Glide.with(this)
                        .load(mPhotoUri)
                        .into(mChosenPhotoImage);

                setLayoutParams(80);
            }
        }
    }


    private void setLayoutParams(int height) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mView.getLayoutParams();

        float pixels =  height * getResources().getDisplayMetrics().density;
        params.height = (int)pixels;
        mView.setLayoutParams(params);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat_activity, menu);
        MenuItem connectDisconnect = menu.findItem(R.id.action_connect_disconnect_in_chat);
        switch (mState) {
            case CONNECTING:
                connectDisconnect.setEnabled(false);
                break;
            case CONNECTED:
                connectDisconnect.setTitle("Disconnect");
                break;
            case DISCONNECTED:
                connectDisconnect.setTitle("Connect");
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_connect_disconnect_in_chat:
                if (!isBound) {
                    Toast.makeText(this, "Bound error, please close and open chat", Toast.LENGTH_SHORT).show();
                    return true;
                }
                switch (mState) {
                    case CONNECTED:
                        mSendMessageBinder.disconnectFrom(mUser.getEndpointId());
                        return true;
                    case DISCONNECTED:
                        mSendMessageBinder.connectTo(mUser.getEndpointId());
                        return true;
                }
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