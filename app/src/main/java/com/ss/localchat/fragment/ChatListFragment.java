package com.ss.localchat.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ss.localchat.R;
import com.ss.localchat.activity.ChatActivity;
import com.ss.localchat.adapter.ChatListAdapter;
import com.ss.localchat.db.entity.Chat;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.preferences.Preferences;
import com.ss.localchat.util.DividerItemDecoration;
import com.ss.localchat.util.Util;
import com.ss.localchat.viewmodel.MessageViewModel;
import com.ss.localchat.viewmodel.UserViewModel;

import java.util.List;
import java.util.UUID;


public class ChatListFragment extends Fragment {

    public static final String FRAGMENT_TITLE = "Chats";


    private MessageViewModel mMessageViewModel;

    public ChatListFragment() {
    }


    public static ChatListFragment newInstance() {
        ChatListFragment fragment = new ChatListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        init(view);
    }

    public void init(View v) {
        mMessageViewModel = ViewModelProviders.of(this).get(MessageViewModel.class);

        final ChatListAdapter chatListAdapter = new ChatListAdapter(getContext());
        chatListAdapter.setOnItemClickListener(new ChatListAdapter.OnItemClickListener() {
            @Override
            public void onClick(User user) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(ChatActivity.USER_EXTRA, user);
                startActivity(intent);
            }

            @Override
            public void onLongClick(User user, View view) {
                showPopup(user, view);
            }
        });

        UUID myUserId = Preferences.getUserId(getActivity().getApplicationContext());

        UserViewModel userViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        userViewModel.getUsersExceptOwner(myUserId).observe(getActivity(), new Observer<List<Chat>>() {
            @Override
            public void onChanged(@Nullable List<Chat> chats) {
                if (chats == null)
                    return;

                chatListAdapter.setUsers(chats);
            }
        });


        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(v.getContext(), Util.dpToPx(getActivity(), 88));

        RecyclerView recyclerView = v.findViewById(R.id.recycler_view_chat_list_fragment);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(chatListAdapter);
    }

    private void showPopup(final User user, View view) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        getActivity().getMenuInflater().inflate(R.menu.popup_menu_chat_list_fragment, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete_chat:
                        mMessageViewModel.clearHistory(user.getId());
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }
}
