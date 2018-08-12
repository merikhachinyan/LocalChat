package com.ss.localchat.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.ss.localchat.R;
import com.ss.localchat.adapter.viewholder.MessagesViewHolder;
import com.ss.localchat.model.User;
import com.ss.localchat.util.CircularTransformation;

import java.util.List;



public class MessagesRecyclerViewAdapter extends RecyclerView.Adapter<MessagesViewHolder> {
    List<User> mUsers;

    public MessagesRecyclerViewAdapter(List<User> users) {
        mUsers = users;
    }

    @Override
    public MessagesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_messages, parent, false);
        MessagesViewHolder holder = new MessagesViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(MessagesViewHolder holder, int position) {
        User mDataUsers = mUsers.get(position);
        holder.getName().setText(mDataUsers.getName());
        holder.getMessage().setText(mDataUsers.getMessage());

        Picasso.get()
                .load(mDataUsers.getProfilePhotoUrl())
                .transform(new CircularTransformation())
                .into(holder.getImgview());


    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

}
