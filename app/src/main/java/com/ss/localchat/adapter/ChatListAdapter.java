package com.ss.localchat.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.ss.localchat.R;
import com.ss.localchat.adapter.viewholder.ChatViewHolder;
import com.ss.localchat.model.User;
import com.ss.localchat.util.CircularTransformation;

import java.util.List;



public class ChatListAdapter extends RecyclerView.Adapter<ChatViewHolder> {

    private OnItemClickListener mListener;

    private List<User> mUsers;

    public ChatListAdapter(List<User> users) {
        mUsers = users;
    }


    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_view, parent, false);
        return new ChatViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.bind(mUsers.get(position));
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onClick(User user);
    }
}
