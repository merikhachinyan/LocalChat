package com.ss.localchat.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ss.localchat.R;
import com.ss.localchat.adapter.viewholder.ChatViewHolder;
import com.ss.localchat.adapter.viewholder.GroupViewHolder;
import com.ss.localchat.db.entity.Chat;
import com.ss.localchat.db.entity.Group;
import com.ss.localchat.db.entity.GroupChat;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.preferences.Preferences;

import java.util.List;
import java.util.UUID;


public class GroupListAdapter extends RecyclerView.Adapter<GroupViewHolder> {

    private OnItemClickListener mListener;

    private List<GroupChat> mGroupChats;

    private UUID myUserId;


    public GroupListAdapter(UUID id) {
        myUserId = id;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_view, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        GroupChat groupChat = mGroupChats.get(position);
        holder.bind(groupChat.group, groupChat.message, groupChat.count, mListener, myUserId);
    }

    @Override
    public int getItemCount() {
        return mGroupChats == null ? 0 : mGroupChats.size();
    }

    public void setGroups(List<GroupChat> groups) {
        mGroupChats = groups ;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onClick(Group group);
        void onLongClick(Group group, View view);
    }
}
