package com.ss.localchat.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ss.localchat.R;
import com.ss.localchat.adapter.MessageListAdapter;
import com.ss.localchat.model.Message;
import com.ss.localchat.model.User;

public class ChatFragment extends Fragment {

    private EditText mMessageInputEditText;

    private MessageListAdapter mMessageListAdapter;

    private User mUser;

    public ChatFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);


            View customView = LayoutInflater.from(getActivity()).inflate(R.layout.chat_fragment_action_bar_custom_view, null);
            actionBar.setCustomView(customView);
        }
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mMessageInputEditText = view.findViewById(R.id.message_input_edit_text_chat_fragment);
        mMessageInputEditText.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        mMessageInputEditText.requestFocus();

        mMessageListAdapter = new MessageListAdapter();

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_chat_fragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mMessageListAdapter);

        ImageButton sendImageButton = view.findViewById(R.id.send_button_chat_fragment);
        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mMessageInputEditText.getText().toString();
                if (!text.isEmpty()) {
                    sendMessage(text);
                    mMessageInputEditText.setText("");
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayShowCustomEnabled(false);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.chat_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search_in_chat:
                Toast.makeText(getActivity(), "Search", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_clear_history_in_chat:
                Toast.makeText(getActivity(), "Clear history", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_delete_in_chat:
                Toast.makeText(getActivity(), "Delete chat", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendMessage(String text) {
        mMessageListAdapter.addMessage(new Message(text, null));
    }
}
