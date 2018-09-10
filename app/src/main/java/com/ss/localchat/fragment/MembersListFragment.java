package com.ss.localchat.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ss.localchat.R;
import com.ss.localchat.activity.ChatActivity;
import com.ss.localchat.adapter.UsersListAdapter;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.util.DividerItemDecoration;
import com.ss.localchat.util.Util;
import com.ss.localchat.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MembersListFragment extends DialogFragment {

    public static final String MEMBERS_LIST_KEY = "members.list";

    private List<UUID> mListOfUsersId = new ArrayList<>();

    public static MembersListFragment newInstance(ArrayList<String> list) {
        MembersListFragment fragment = new MembersListFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(MEMBERS_LIST_KEY, list);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_members_list, null);

        ArrayList<String> usersIdStringList = null;
        if (getArguments() != null) {
            usersIdStringList = getArguments().getStringArrayList(MEMBERS_LIST_KEY);
        }
        if (usersIdStringList != null) {
            for (String idString : usersIdStringList) {
                mListOfUsersId.add(UUID.fromString(idString));
            }
        }

        final UsersListAdapter usersListAdapter = new UsersListAdapter();
        usersListAdapter.setOnClickListener(new UsersListAdapter.OnClickListener() {
            @Override
            public void onClick(User user) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(ChatActivity.USER_EXTRA, user);
                startActivity(intent);
            }
        });

        UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        userViewModel.getUsersListById(mListOfUsersId.toArray(new UUID[mListOfUsersId.size()])).observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(@Nullable List<User> users) {
                usersListAdapter.setUsers(users);
            }
        });


        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_members_list_fragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), Util.dpToPx(getActivity(), 80)));
        recyclerView.setAdapter(usersListAdapter);

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle("Members")
                .create();
    }
}
