package com.ss.localchat.activity;

import android.os.Bundle;
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
import com.ss.localchat.util.CircularTransformation;

public class ChatActivity extends AppCompatActivity {

    public static final String USER_EXTRA = "chat.user";
    public static final String NEW_USER_EXTRA = "new.user";


    private EditText mMessageInputEditText;

    private MessageListAdapter mMessageListAdapter;

    private User mUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getIntent() != null) {
             mUser = (User) getIntent().getSerializableExtra(NEW_USER_EXTRA);
             if(mUser != null){
                 Toast.makeText(this, "New User", Toast.LENGTH_SHORT).show();
             } else {
                 mUser = (User) getIntent().getSerializableExtra(USER_EXTRA);
             }
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
                String text = mMessageInputEditText.getText().toString().trim();
                if (!text.isEmpty()) {
                    sendMessage(text);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendMessage(String text) {
        mMessageListAdapter.addMessage(new Message(text, null));
    }
}
