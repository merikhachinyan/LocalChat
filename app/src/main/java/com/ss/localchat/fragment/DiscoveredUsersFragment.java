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
import android.support.design.widget.FloatingActionButton;
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

import com.ss.localchat.R;
import com.ss.localchat.activity.ChatActivity;
import com.ss.localchat.activity.MainActivity;
import com.ss.localchat.adapter.DiscoveredUsersListAdapter;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.service.ChatService;

public class DiscoveredUsersFragment extends Fragment {

    public static final String FRAGMENT_TITLE = "Discover";

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


    private MainActivity.OnButtonClickListener mListener = new MainActivity.OnButtonClickListener() {
        @Override
        public void onDiscoveryButtonClick(FloatingActionButton fab) {
            if (mDiscoverBinder.isRunningService()) {
                if (!mDiscoverBinder.isRunningDiscovery()) {
                    startDiscovery();
                    fab.setImageResource(R.drawable.ic_stop_black_24dp);
                } else {
                    stopDiscovery();
                    fab.setImageResource(R.drawable.ic_bluetooth_search_start);
                }

                mDiscoveredUsersListAdapter.showLoadingIndicator(mDiscoverBinder.isRunningDiscovery());

            } else {
                createDialog().show();
            }
        }
    };

    private DiscoveredUsersListAdapter mDiscoveredUsersListAdapter;

    private ChatService.ServiceBinder mDiscoverBinder;

    private OnStartDiscoveryListener mDiscoveryListener;

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
        setHasOptionsMenu(true);

    }

    @Override
    public void onResume() {
        super.onResume();

        if (isBound) {
            mDiscoveredUsersListAdapter.showLoadingIndicator(mDiscoverBinder.isRunningDiscovery());
            if (mDiscoveryListener != null) {
                mDiscoveryListener.OnStartDiscovery(mDiscoverBinder.isRunningDiscovery());
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

        final RecyclerView recyclerView = view.findViewById(R.id.recycler_view_discovered_users_fragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mDiscoveredUsersListAdapter);

        ((MainActivity) getActivity()).setOnButtonClickListener(mListener);
    }

    private void startDiscovery() {
        if (isBound) {
            mDiscoverBinder.startDiscovery();
        }
    }

    private void stopDiscovery() {
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

                        if (isBound) {
                            mDiscoveryListener.OnStartDiscovery(mDiscoverBinder.isRunningDiscovery());

                            mDiscoveredUsersListAdapter.showLoadingIndicator(mDiscoverBinder.isRunningDiscovery());
                        }
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

    public void setOnStartDiscoveryListener(OnStartDiscoveryListener listener) {
        mDiscoveryListener = listener;
    }

    public interface OnStartDiscoveryListener {
        void OnStartDiscovery(boolean flag);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menu.clear();
        menuInflater.inflate(R.menu.menu_main_search, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        search(searchView);
    }

    private void search(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mDiscoveredUsersListAdapter != null) {
                    mDiscoveredUsersListAdapter.getFilter(newText);
                }

                return true;
            }
        });

    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getView() != null) {
                init(getView());
                final InputMethodManager imm = (InputMethodManager) getView().getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                }

            }
        }
    }

}
