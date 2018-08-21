package com.ss.localchat.adapter;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ss.localchat.R;
import com.ss.localchat.adapter.viewholder.ReceivedMessageHolder;
import com.ss.localchat.adapter.viewholder.SentMessageHolder;
import com.ss.localchat.db.entity.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int RECEIVED_MESSAGE_TYPE = 1;

    private static final int SENT_MESSAGE_TYPE = 2;

    private static final int DATE_TYPE = 3;


    private List<Message> mMessages;

    private Context mContext;

    public MessageListAdapter(Context context) {
        mContext = context;
        mMessages = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case RECEIVED_MESSAGE_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.received_message_item_view, parent, false);
                return new ReceivedMessageHolder(view);
            case SENT_MESSAGE_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sent_message_item_view, parent, false);
                return new SentMessageHolder(view);
            default:
                throw new IllegalArgumentException("No such input type in RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case RECEIVED_MESSAGE_TYPE:
                ((ReceivedMessageHolder) holder).bind(mMessages.get(position));
                break;
            case SENT_MESSAGE_TYPE:
                ((SentMessageHolder) holder).bind(mMessages.get(position));
                break;
            default:
                throw new IllegalArgumentException("No such input type in RecyclerView");
        }
    }

    @Override
    public int getItemCount() {
        return mMessages == null ? 0 : mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        UUID ownerId = UUID.fromString(PreferenceManager.getDefaultSharedPreferences(mContext).getString("id", ""));
        Message message = mMessages.get(position);
        if (message.getSenderId().equals(ownerId)) {
            return SENT_MESSAGE_TYPE;
        } else {
            return RECEIVED_MESSAGE_TYPE;
        }
    }

    public void setMessages(List<Message> messages) {
        mMessages = messages;
        notifyDataSetChanged();
    }

    public void addMessages(List<Message> messages) {
        int startPosition = mMessages.size();
        mMessages.addAll(messages);
        notifyItemRangeInserted(startPosition, messages.size());
    }

    public void addMessage(Message message) {
        mMessages.add(message);
        notifyItemInserted(mMessages.size() - 1);
    }
}
