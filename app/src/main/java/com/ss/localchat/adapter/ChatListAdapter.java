package com.ss.localchat.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ss.localchat.R;
import com.ss.localchat.adapter.viewholder.ChatViewHolder;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.db.entity.Chat;

import java.util.List;


public class ChatListAdapter extends RecyclerView.Adapter<ChatViewHolder> {

    private OnItemClickListener mListener;

    private List<Chat> mChats;

    private Context mContext;


    public ChatListAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_view, parent, false);
        return new ChatViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat user = mChats.get(position);
        holder.bind(user.user, user.message, user.count, mContext);
    }

    @Override
    public int getItemCount() {
        return mChats == null ? 0 : mChats.size();
    }

    public void setUsers(List<Chat> chats) {
        mChats = chats;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onClick(User user);
        void onLongClick(User user, View view);
    }
}
