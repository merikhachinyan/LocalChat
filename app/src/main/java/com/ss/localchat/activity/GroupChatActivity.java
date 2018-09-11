package com.ss.localchat.activity;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.rockerhieu.emojicon.EmojiconEditText;
import com.ss.localchat.R;
import com.ss.localchat.adapter.MessageListAdapter;
import com.ss.localchat.adapter.UsersListAdapter;
import com.ss.localchat.db.entity.Group;
import com.ss.localchat.db.entity.Message;
import com.ss.localchat.fragment.MembersListFragment;
import com.ss.localchat.helper.NotificationHelper;
import com.ss.localchat.preferences.Preferences;
import com.ss.localchat.service.ChatService;
import com.ss.localchat.view.EmojiKeyboardLayout;
import com.ss.localchat.viewmodel.GroupViewModel;
import com.ss.localchat.viewmodel.MessageViewModel;
import com.ss.localchat.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class GroupChatActivity extends AppCompatActivity {

    public static final String GROUP_EXTRA = "chat.user";

    public static final int REQUEST_CODE_CHOOSE_PICTURE = 3;


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mSendMessageBinder = (ChatService.ServiceBinder) service;
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mSendMessageBinder = null;
            isBound = false;
        }
    };

    public static boolean isCurrentlyRunning;

    public static UUID currentGroupId;


    private EmojiconEditText mMessageInputEditText;

    private MessageListAdapter mMessageListAdapter;


    private ChatService.ServiceBinder mSendMessageBinder;

    private boolean isBound;


    private MessageViewModel mMessageViewModel;

    private UserViewModel mUserViewModel;

    GroupViewModel mGroupViewModel;


    private Group mGroup;

    private List<String> mEndpointList;

    private List<UUID> mListOfUsersId;

    private Uri mPhotoUri;


    private TextView groupInfo;

    private TextView groupName;

    private ImageView mChosenPhotoImage;

    private ImageView mRemovePhotoImage;

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

            ImageView groupPhoto = actionBarView.findViewById(R.id.user_circle_image_view_on_toolbar);
            groupPhoto.setVisibility(View.GONE);
            groupName = actionBarView.findViewById(R.id.user_name_text_view_on_toolbar);
            groupInfo = actionBarView.findViewById(R.id.user_info_text_view_on_toolbar);

            actionBarView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<String> stringListOfId = new ArrayList<>();
                    for (UUID id : mListOfUsersId) {
                        stringListOfId.add(id.toString());
                    }
                    MembersListFragment dialog = MembersListFragment.newInstance(stringListOfId);
                    dialog.show(getSupportFragmentManager(), "members.fragment.tag");
                }
            });

            actionBar.setCustomView(actionBarView);
        }

        if (getIntent() != null) {
            mGroup = (Group) getIntent().getSerializableExtra(GROUP_EXTRA);
            currentGroupId = mGroup.getId();
        }

        bindService(new Intent(this, ChatService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(mServiceConnection);
        }
    }

    private void initActionBar() {
        groupName.setText(mGroup.getName());

        int size = mListOfUsersId.size();
        String info = size + (size == 1 ? " member" : " members");
        groupInfo.setText(info);
    }

    private void init() {
        mMessageInputEditText = findViewById(R.id.message_input_edit_text_chat_activity);
        mMessageInputEditText.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        mMessageInputEditText.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

        mGroupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);
        mGroupViewModel.getGroupById(mGroup.getId()).observe(this, new Observer<Group>() {
            @Override
            public void onChanged(@Nullable Group group) {
                if (group != null) {
                    List<UUID> uuidList = new ArrayList<>();
                    mGroup = group;
                    String[] members = group.getMembers().split(";");
                    for (String member : members) {
                        uuidList.add(UUID.fromString(member));
                    }
                    mListOfUsersId = uuidList;
                    initActionBar();
                    mUserViewModel.getEndpointId(uuidList.toArray(new UUID[uuidList.size()])).observe(GroupChatActivity.this, new Observer<List<String>>() {
                        @Override
                        public void onChanged(@Nullable List<String> strings) {
                            if (strings != null) {
                                strings.remove(null);
                                mEndpointList = strings;
                            }
                        }
                    });
                }
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

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mMessageListAdapter);
        EmojiKeyboardLayout keyboardLayout = findViewById(R.id.keyboardLayout);
        keyboardLayout.setup(this, recyclerView);

        NotificationHelper.getManager(this).cancel(mGroup.getId().toString(), NotificationHelper.MESSAGE_NOTIFICATION_ID);


        mMessageViewModel = ViewModelProviders.of(this).get(MessageViewModel.class);

        final LiveData<List<Message>> messagesLiveData = mMessageViewModel.getReadOrUnreadMessagesWith(mGroup.getId(), true, true);
        messagesLiveData.observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(@Nullable List<Message> messages) {
                mMessageListAdapter.addMessages(messages);
                messagesLiveData.removeObserver(this);

                mMessageViewModel.getReadOrUnreadMessagesWith(mGroup.getId(), false, true).observe(GroupChatActivity.this, new Observer<List<Message>>() {
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
                if (!messageText.isEmpty()) {
                    sendMessage(messageText);
                    mMessageInputEditText.setText("");
                }
            }
        });

        mView = findViewById(R.id.divider_view_chat_activity);

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

        findViewById(R.id.attach_photo_chat_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhoto();
            }
        });
    }

    private void sendMessage(String text) {
        if (!mGroup.getMembers().contains(Preferences.getUserId(getApplicationContext()).toString())) {
            Toast.makeText(this, "You've left the group!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isBound) {
            mSendMessageBinder.sendGroupMessageTo(mEndpointList, text, mGroup);

            UUID myUserId = Preferences.getUserId(getApplicationContext());
            String myUserName = Preferences.getUserName(getApplicationContext());

            Message message = new Message();
            message.setText(text);
            message.setRead(false);
            message.setReceiverId(mGroup.getId());
            message.setSenderId(myUserId);
            message.setGroup(true);
            message.setSenderName(myUserName);
            mMessageViewModel.insert(message);
        }
    }


    private void leaveGroup() {

        if (isBound) {
            String members = mGroup.getMembers();

            if (members.contains(Preferences.getUserId(getApplicationContext()).toString().concat(";"))) {
                members = members.replace(Preferences.getUserId(getApplicationContext()).toString().concat(";"), "");
            } else {
                members = members.replace(";".concat(Preferences.getUserId(getApplicationContext()).toString()), "");
            }
            mGroup.setMembers(members);
            mGroupViewModel.update(mGroup);
            invalidateOptionsMenu();

            mSendMessageBinder.sendGroupTo(mEndpointList, mGroup);
            mSendMessageBinder.sendGroupLeaveMessageTo(mEndpointList, mGroup);

            UUID myUserId = Preferences.getUserId(getApplicationContext());
            String sender = Preferences.getUserName(getApplicationContext()) + " leaves the group";

            Message message = new Message();
            message.setText(sender);
            message.setRead(false);
            message.setReceiverId(mGroup.getId());
            message.setSenderId(myUserId);
            message.setGroup(true);
            mMessageViewModel.insert(message);
        }
    }

    private void choosePhoto() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_CHOOSE_PICTURE);
    }

    private void setLayoutParams(int height) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mView.getLayoutParams();

        float pixels = height * getResources().getDisplayMetrics().density;
        params.height = (int) pixels;
        mView.setLayoutParams(params);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_chat_activity, menu);
        MenuItem menuItem = menu.findItem(R.id.action_leave_in_group);
        if (!mGroup.getMembers().contains(Preferences.getUserId(getApplicationContext()).toString())) {
            menuItem.setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_leave_in_group:
                leaveGroup();
                return true;
            case R.id.action_clear_history_in_group:
                mMessageViewModel.clearHistory(mGroup.getId());
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