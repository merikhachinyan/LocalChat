package com.ss.localchat.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ss.localchat.R;
import com.ss.localchat.activity.ChatActivity;
import com.ss.localchat.adapter.DiscoveredUsersListAdapter;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.service.ChatService;

public class DiscoveredUsersFragment extends Fragment {

    public static final String FRAGMENT_TITLE = "Discover";


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDiscoverBinder = (ChatService.ServiceBinder) service;

            mDiscoverBinder.setOnDiscoverUsersListener(mOnDiscoverUsersListener);
//            mDiscoverBinder.startDiscovery();

            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDiscoverBinder = null;

            isBound = false;
        }
    };

    private ChatService.OnDiscoverUsersListener mOnDiscoverUsersListener =
            new ChatService.OnDiscoverUsersListener() {
                @Override
                public void OnUserFound(User user) {
                    mDiscoveredUsersListAdapter.addUser(user);
                }

                @Override
                public void onUserLost(String id) {
                    mDiscoveredUsersListAdapter.removeUserById(id);
                }
            };

    private Button mStartDiscoverButton;

    private DiscoveredUsersListAdapter mDiscoveredUsersListAdapter;

    private ChatService.ServiceBinder mDiscoverBinder;

    private boolean isBound;

    public DiscoveredUsersFragment() {

    }

    public static DiscoveredUsersFragment newInstance() {
        DiscoveredUsersFragment fragment = new DiscoveredUsersFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().bindService(new Intent(getActivity(), ChatService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discovered_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        init(view);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (isBound) {
            mDiscoverBinder.stopDiscovery();

            getActivity().unbindService(mServiceConnection);

            Log.v("___", "unbind discover");
        }
    }

    private void init(View view) {
        mDiscoveredUsersListAdapter = new DiscoveredUsersListAdapter();
        mDiscoveredUsersListAdapter.setOnItemClickListener(new DiscoveredUsersListAdapter.OnItemClickListener() {
            @Override
            public void onClick(User user) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(ChatActivity.USER_EXTRA, user);
                startActivity(intent);
            }
        });

        final RecyclerView recyclerView = view.findViewById(R.id.recycler_view_discovered_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mDiscoveredUsersListAdapter);

        mStartDiscoverButton = view.findViewById(R.id.start_discover_users_button);
        mStartDiscoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartDiscoverButton.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                if (isBound) {
                    mDiscoverBinder.startDiscovery();
                }
//                getActivity().bindService(new Intent(getActivity(), ChatService.class),
//                        mServiceConnection, Context.BIND_AUTO_CREATE);
            }
        });
    }
}
