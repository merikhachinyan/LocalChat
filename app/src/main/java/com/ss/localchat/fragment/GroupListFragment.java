package com.ss.localchat.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.ss.localchat.R;
import com.ss.localchat.activity.GroupChatActivity;
import com.ss.localchat.adapter.GroupListAdapter;
import com.ss.localchat.db.entity.Group;
import com.ss.localchat.db.entity.GroupChat;
import com.ss.localchat.preferences.Preferences;
import com.ss.localchat.util.DividerItemDecoration;
import com.ss.localchat.util.Util;
import com.ss.localchat.viewmodel.GroupViewModel;
import com.ss.localchat.viewmodel.MessageViewModel;

import java.util.List;

public class GroupListFragment extends Fragment {

    public static final String FRAGMENT_TITLE = "Groups";

    private GroupViewModel mGroupViewModel;

    private MessageViewModel mMessageViewModel;
    private GroupListAdapter groupListAdapter;
    public GroupListFragment() {

    }

    public static GroupListFragment newInstance() {
        GroupListFragment fragment = new GroupListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        init(view);
        setHasOptionsMenu(true);
    }

    private void init(View view) {
        groupListAdapter = new GroupListAdapter(Preferences.getUserId(getContext().getApplicationContext()));
        groupListAdapter.setOnItemClickListener(new GroupListAdapter.OnItemClickListener() {
            @Override
            public void onClick(Group group) {
                Intent intent = new Intent(getActivity(), GroupChatActivity.class);
                intent.putExtra(GroupChatActivity.GROUP_EXTRA, group);
                startActivity(intent);
            }

            @Override
            public void onLongClick(Group group, View view) {
                showPopup(group, view);
            }
        });

        mMessageViewModel = ViewModelProviders.of(this).get(MessageViewModel.class);

        mGroupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);
        mGroupViewModel.getGroups().observe(this, new Observer<List<GroupChat>>() {
            @Override
            public void onChanged(@Nullable List<GroupChat> groupChats) {
                if (groupChats == null) {
                    return;
                }

                groupListAdapter.setGroups(groupChats);
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_group_list_fragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), Util.dpToPx(getActivity(), 80)));
        recyclerView.setAdapter(groupListAdapter);
    }

    private void showPopup(final Group group, View view) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        getActivity().getMenuInflater().inflate(R.menu.popup_menu_group_list_fragment, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete_group:
                        if (group.getMembers().contains(Preferences.getUserId(getActivity().getApplicationContext()).toString())) {
                            Toast.makeText(getActivity(), "Leave the group first", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        mGroupViewModel.delete(group);
                        mMessageViewModel.clearHistory(group.getId());
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
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
                if (groupListAdapter != null) {
                    groupListAdapter.getFilter(newText);
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
