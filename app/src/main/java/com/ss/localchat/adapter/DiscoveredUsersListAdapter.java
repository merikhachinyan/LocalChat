package com.ss.localchat.adapter;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ss.localchat.R;
import com.ss.localchat.adapter.viewholder.DiscoveredUserHolder;
import com.ss.localchat.adapter.viewholder.LoadingIndicatorViewHolder;
import com.ss.localchat.db.entity.GroupChat;
import com.ss.localchat.db.entity.Message;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.viewmodel.MessageViewModel;

import java.util.ArrayList;
import java.util.List;

public class DiscoveredUsersListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int DISCOVERED_USER_TYPE = 1;

    private static final int LOADING_INDICATOR_TYPE = 2;


    private OnItemClickListener mListener;

    private List<User> mUsers;
    private List<User> filterUsers;

    public DiscoveredUsersListAdapter() {
        mUsers = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case DISCOVERED_USER_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.discovered_user_item_view, parent, false);
                return new DiscoveredUserHolder(view, mListener);
            case LOADING_INDICATOR_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_indicator_item_view, parent, false);
                return new LoadingIndicatorViewHolder(view);
            default:
                throw new IllegalArgumentException("No such input type in RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == DISCOVERED_USER_TYPE) {
            User user = mUsers.get(position);
            ((DiscoveredUserHolder) holder).bind(user);
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mUsers.size() - 1 && mUsers.get(position) == null)
            return LOADING_INDICATOR_TYPE;
        else
            return DISCOVERED_USER_TYPE;
    }

    public void addUser(User user) {
        int indexGap = mUsers.size() > 0 && mUsers.get(mUsers.size() - 1) == null ? 1 : 0;
        mUsers.add(mUsers.size() - indexGap, user);
        notifyItemInserted(mUsers.size() - indexGap - 1);
        filterUsers = mUsers;
    }

    public void removeUserById(String id) {
        for (User user : mUsers) {
            if (id.equals(user.getEndpointId())) {
                int index = mUsers.indexOf(user);
                mUsers.remove(user);
                notifyItemRemoved(index);
                break;
            }
        }
    }

    public void showLoadingIndicator(boolean flag) {
        if (flag) {
            if (mUsers.size() == 0 || mUsers.get(mUsers.size() - 1) != null) {
                mUsers.add(null);
                notifyItemInserted(mUsers.size() - 1);
            }
        } else {
            if (mUsers.size() != 0) {
                int i = mUsers.size() - 1;
                if (mUsers.get(i) == null) {
                    mUsers.remove(i);
                    notifyItemRemoved(i);
                }
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onClick(User user);
    }

    private ArrayList<User> filter(List<User> models, String query) {

        final String lowerCaseQuery = query.toLowerCase();

        final ArrayList<User> filteredModelList = new ArrayList<>();
        for (final User model : models) {
            if (model != null) {
                final String name = model.getName().toLowerCase();
                final String endpointId = model.getId().toString().toLowerCase();
                if (name.contains(lowerCaseQuery) | endpointId.contains(lowerCaseQuery)) {
                    filteredModelList.add(model);
                }
            }
        }
        return filteredModelList;
    }

    public void getFilter(String str) {
        if (filterUsers != null) {
            mUsers = filter(filterUsers, str);
            notifyDataSetChanged();
        }

    }
}