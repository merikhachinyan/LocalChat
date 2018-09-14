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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.ss.localchat.R;
import com.ss.localchat.activity.ChatActivity;
import com.ss.localchat.adapter.ChatListAdapter;
import com.ss.localchat.db.entity.Chat;
import com.ss.localchat.db.entity.Message;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.preferences.Preferences;
import com.ss.localchat.util.DividerItemDecoration;
import com.ss.localchat.util.Util;
import com.ss.localchat.viewmodel.MessageViewModel;
import com.ss.localchat.viewmodel.UserViewModel;

import java.util.List;
import java.util.UUID;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.support.constraint.Constraints.TAG;


public class ChatListFragment extends Fragment {

    public static final String FRAGMENT_TITLE = "Chats";

    private ChatListAdapter mChatListAdapter;
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
        setHasOptionsMenu(true);
    }

    public void init(View v) {
        mMessageViewModel = ViewModelProviders.of(this).get(MessageViewModel.class);

        mChatListAdapter = new ChatListAdapter(getContext());
        mChatListAdapter.setOnItemClickListener(new ChatListAdapter.OnItemClickListener() {
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
        userViewModel.getChatsExceptOwner(myUserId).observe(getActivity(), new Observer<List<Chat>>() {
            @Override
            public void onChanged(@Nullable List<Chat> chats) {
                if (chats == null)
                    return;

                mChatListAdapter.setUsers(chats);
            }
        });


        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(v.getContext(), Util.dpToPx(getActivity(), 80));

        RecyclerView recyclerView = v.findViewById(R.id.recycler_view_chat_list_fragment);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(mChatListAdapter);
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

    private SearchView searchView;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menu.clear();
        menuInflater.inflate(R.menu.menu_main_search, menu);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        search(searchView);
    }

    private void search(final SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mChatListAdapter != null) {
                    mChatListAdapter.getFilter(newText);
                }

                return true;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
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
