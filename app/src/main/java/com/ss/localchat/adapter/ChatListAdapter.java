package com.ss.localchat.adapter;

import android.content.Context;
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
import com.ss.localchat.adapter.viewholder.ChatViewHolder;
import com.ss.localchat.db.entity.Chat;
import com.ss.localchat.db.entity.Message;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.preferences.Preferences;
import com.ss.localchat.viewmodel.MessageViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ChatListAdapter extends RecyclerView.Adapter<ChatViewHolder> {

    private List<Chat> mFilteredList = new ArrayList<>();
    private OnItemClickListener mListener;
    private MessageViewModel mMessageViewModel;
    private List<Chat> mChats = new ArrayList<>();

    private Context mContext;
    private View view;


    public ChatListAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_view, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat user = mFilteredList.get(position);
        holder.bind(user.user, user.message, user.count, mListener, mContext);
    }

    @Override
    public int getItemCount() {
        return mFilteredList == null ? 0 : mFilteredList.size();
    }

    public void setUsers(List<Chat> chats) {
        mChats = chats;

        // TODO: 8/27/2018 change users set logic, should pass users list instead of user
        mFilteredList.clear();
        mFilteredList = chats;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    private List<Message> messageList;

    private ArrayList<Chat> filter(List<Chat> models, String query) {

        final String lowerCaseQuery = query.toLowerCase();

        final ArrayList<Chat> filteredModelList = new ArrayList<>();
        for (Chat model : models) {
            mMessageViewModel = ViewModelProviders.of((FragmentActivity) view.getContext()).get(MessageViewModel.class);

            mMessageViewModel.getMessagesWith(model.user.getId()).observe((FragmentActivity) view.getContext(), new Observer<List<Message>>() {
                @Override
                public void onChanged(@Nullable List<Message> messages) {
                    messageList = messages;
                }
            });

            final String name = model.user.getName().toLowerCase();
            final String message = model.message.getText().toLowerCase();
            if (name.contains(lowerCaseQuery) | message.contains(lowerCaseQuery)) {
                filteredModelList.add(model);
            } else if (messageList != null) {
                for (int i = 0; i < messageList.size(); i++) {
                    if (messageList.get(i).getText().toLowerCase().contains(lowerCaseQuery)) {
                        filteredModelList.add(model);
                    }
                }
            }
        }
        return filteredModelList;
    }

    public void getFilter(String str) {

        mFilteredList = filter(mChats, str);
        notifyDataSetChanged();

    }

    public interface OnItemClickListener {
        void onClick(User user);
        void onLongClick(User user, View view);
    }
}
