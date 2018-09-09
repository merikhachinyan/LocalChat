package com.ss.localchat.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ss.localchat.R;
import com.ss.localchat.adapter.viewholder.NewGroupUserViewHolder;
import com.ss.localchat.db.entity.User;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class NewGroupUsersListAdapter extends RecyclerView.Adapter<NewGroupUserViewHolder> {

    private List<User> mUsers;

    private Set<User> mCheckedUsers = new LinkedHashSet<>();

    private OnClickListener mListener = new OnClickListener() {
        @Override
        public void onClick(User user) {
            if (!mCheckedUsers.contains(user)) {
                mCheckedUsers.add(user);
            } else {
                mCheckedUsers.remove(user);
            }
            notifyDataSetChanged();
        }
    };

    @NonNull
    @Override
    public NewGroupUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_group_user_item_view, parent, false);
        return new NewGroupUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewGroupUserViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.bind(user, mCheckedUsers.contains(user), mListener);
    }

    @Override
    public int getItemCount() {
        return mUsers == null ? 0 : mUsers.size();
    }

    public void setUsers(List<User> users) {
        mUsers = users;
        notifyDataSetChanged();
    }

    public Set<User> getCheckedUsers() {
        return mCheckedUsers;
    }

    public interface OnClickListener {
        void onClick(User user);
    }
}
