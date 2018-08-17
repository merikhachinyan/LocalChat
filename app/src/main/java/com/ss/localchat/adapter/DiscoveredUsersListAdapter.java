package com.ss.localchat.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ss.localchat.R;
import com.ss.localchat.adapter.viewholder.DiscoveredUserHolder;
import com.ss.localchat.model.User;

import java.util.ArrayList;
import java.util.List;

public class DiscoveredUsersListAdapter extends RecyclerView.Adapter<DiscoveredUserHolder>{

    private OnItemClickListener mListener;

    private List<User> mUsers;

    public DiscoveredUsersListAdapter() {
        mUsers = new ArrayList<>();
    }

    @NonNull
    @Override
    public DiscoveredUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.discovered_user_item_view, parent, false);
        return new DiscoveredUserHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscoveredUserHolder holder, int position) {
        User user = mUsers.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void addUser(User user){
        mUsers.add(user);
        notifyItemInserted(mUsers.size() - 1);
    }

    public void removeUserById(String id){
        for (User user : mUsers){
            if(id.equals(user.getId())){
                mUsers.remove(user);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public interface OnItemClickListener{
        void onClick(User user);
    }
}