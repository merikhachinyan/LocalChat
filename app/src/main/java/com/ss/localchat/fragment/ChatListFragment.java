package com.ss.localchat.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ss.localchat.R;
import com.ss.localchat.activity.ChatActivity;
import com.ss.localchat.adapter.ChatListAdapter;
import com.ss.localchat.db.entity.Message;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.util.DividerItemDecoration;
import com.ss.localchat.util.Util;
import com.ss.localchat.viewmodel.MessageViewModel;
import com.ss.localchat.viewmodel.UserViewModel;

import java.util.List;
import java.util.UUID;


public class ChatListFragment extends Fragment {

    public static final String FRAGMENT_TITLE = "Chats";

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
        final ChatListAdapter chatListAdapter = new ChatListAdapter(getActivity());
        chatListAdapter.setOnItemClickListener(new ChatListAdapter.OnItemClickListener() {
            @Override
            public void onClick(User user) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(ChatActivity.USER_EXTRA, user);
                startActivity(intent);
            }
        });

        UserViewModel userViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        final MessageViewModel messageViewModel = ViewModelProviders.of(getActivity()).get(MessageViewModel.class);

        final UUID userId = UUID.fromString(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("user.id", ""));

        userViewModel.getUsersExceptOwner(userId).observe(getActivity(), new Observer<List<User>>() {
            @Override
            public void onChanged(@Nullable List<User> users) {
                if (users == null || users.size() == 0)
                    return;

                chatListAdapter.setUsers(users);
                for (final User user : users) {
                    messageViewModel.getMessagesWith(user.getId()).observe(getActivity(), new Observer<List<Message>>() {
                        @Override
                        public void onChanged(@Nullable List<Message> messages) {
                            if (messages == null || messages.size() == 0)
                                return;

                            int unreadMessagesCount = 0;
                            int i = messages.size() - 1;
                            while (!messages.get(i).isRead()) {
                                unreadMessagesCount++;
                                i--;
                            }
                            chatListAdapter.setUnreadMessagesCount(user, unreadMessagesCount);
                            chatListAdapter.setLastMessage(user, messages.get(messages.size() - 1));
                        }
                    });
                }
            }
        });

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(v.getContext(), Util.dpToPx(getActivity(), 88));

        RecyclerView recyclerView = v.findViewById(R.id.recycler_view_chat_list_fragment);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(chatListAdapter);
    }
}
