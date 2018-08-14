package com.ss.localchat.fragment;

import android.content.Intent;
import android.os.Bundle;
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
import com.ss.localchat.model.User;
import com.ss.localchat.util.DividerItemDecoration;
import com.ss.localchat.util.Util;

import java.util.ArrayList;
import java.util.List;


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
        List<User> userList = new ArrayList<>();
        User user = new User(null,"Rob Sargsyan", "https://techcrunch.com/wp-content/uploads/2018/03/gettyimages-705351545.jpg?w=1390&crop=1");
        User user1 = new User(null, "Meri Khachinyan", null);
        User user2 = new User(null, "Sergey Kudryashov", "https://pp.userapi.com/c631219/v631219392/147d3/DB7c8X31Xys.jpg");
        userList.add(user);
        userList.add(user1);
        userList.add(user2);

        ChatListAdapter adapter = new ChatListAdapter(userList);
        adapter.setOnItemClickListener(new ChatListAdapter.OnItemClickListener() {
            @Override
            public void onClick(User user) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(ChatActivity.USER_EXTRA, user);
                startActivity(intent);
            }
        });

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(v.getContext(), Util.dpToPx(getActivity(), 88));

        RecyclerView recyclerView = v.findViewById(R.id.recycler_view_chat_list_fragment);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(adapter);
    }
}
