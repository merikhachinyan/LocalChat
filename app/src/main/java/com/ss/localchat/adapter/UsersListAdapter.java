package com.ss.localchat.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ss.localchat.R;
import com.ss.localchat.adapter.viewholder.NewGroupUserViewHolder;
import com.ss.localchat.adapter.viewholder.UserViewHolder;
import com.ss.localchat.db.entity.User;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class UsersListAdapter extends RecyclerView.Adapter<UserViewHolder> {

    private List<User> mUsers;

    private OnClickListener mListener;


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_view, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.bind(user, mListener);
    }

    @Override
    public int getItemCount() {
        return mUsers == null ? 0 : mUsers.size();
    }

    public void setUsers(List<User> users) {
        mUsers = users;
        notifyDataSetChanged();
    }

    public void setOnClickListener(UsersListAdapter.OnClickListener listener) {
        mListener = listener;
    }

    public interface OnClickListener {
        void onClick(User user);
    }
}
