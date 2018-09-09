package com.ss.localchat.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.ss.localchat.R;
import com.ss.localchat.activity.ChatActivity;
import com.ss.localchat.adapter.DiscoveredUsersListAdapter;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.service.ChatService;

public class DiscoveredUsersFragment extends Fragment {

    public static final String FRAGMENT_TITLE = "Discover";

    public static final String START_DISCOVERY = "Start";

    public static final String STOP_DISCOVERY = "Stop";

    public static final String DIALOG_TEXT = "Enable Advertising ?";

    public static final String ENABLE = "Enable";

    public static final String CANCEL = "Cancel";


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDiscoverBinder = (ChatService.ServiceBinder) service;

            mDiscoverBinder.setOnDiscoverUsersListener(mOnDiscoverUsersListener);

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
   private InputMethodManager imm;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        return inflater.inflate(R.layout.fragment_discovered_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        init(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isBound) {
            mDiscoveredUsersListAdapter.showLoadingIndicator(mDiscoverBinder.isRunningDiscovery());

            if (mDiscoverBinder.isRunningDiscovery()) {
                mStartDiscoverButton.setText(STOP_DISCOVERY);
            } else {
                mStartDiscoverButton.setText(START_DISCOVERY);
            }
        }
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
        recyclerView.setAdapter(mDiscoveredUsersListAdapter);

        mStartDiscoverButton = view.findViewById(R.id.start_discover_users_button);
        mStartDiscoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDiscoverBinder.isRunningService()) {

                    if (!mDiscoverBinder.isRunningDiscovery()) {
                        startDiscovery();
                    } else {
                        stopDiscovery();
                    }

                    mDiscoveredUsersListAdapter.showLoadingIndicator(mDiscoverBinder.isRunningDiscovery());

                } else {
                    createDialog().show();
                }
            }
        });
    }

    private void startDiscovery() {
        mStartDiscoverButton.setText(STOP_DISCOVERY);

        if (isBound) {
            mDiscoverBinder.startDiscovery();
        }
    }

    private void stopDiscovery() {
        mStartDiscoverButton.setText(START_DISCOVERY);

        if (isBound) {
            mDiscoverBinder.stopDiscovery();
        }
    }

    private Dialog createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(DIALOG_TEXT)
                .setPositiveButton(ENABLE, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().startService(new Intent(getActivity(), ChatService.class));

                        startDiscovery();

                        mDiscoveredUsersListAdapter.showLoadingIndicator(mDiscoverBinder.isRunningDiscovery());
                    }
                })
                .setNegativeButton(CANCEL, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menu.clear();
        menuInflater.inflate(R.menu.menu_main_search, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.requestFocus();
    }
}
