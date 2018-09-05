package com.ss.localchat.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ss.localchat.R;
import com.ss.localchat.adapter.viewholder.DateViewHolder;
import com.ss.localchat.adapter.viewholder.ReceivedMessageHolder;
import com.ss.localchat.adapter.viewholder.SentMessageHolder;
import com.ss.localchat.db.entity.Message;
import com.ss.localchat.preferences.Preferences;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class MessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int RECEIVED_MESSAGE_TYPE = 1;

    private static final int SENT_MESSAGE_TYPE = 2;

    private static final int DATE_TYPE = 3;


    private List<Message> mMessages;

    private Context mContext;

    private OnImageClickListener mListener;

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
                return new ReceivedMessageHolder(view, mListener);
            case SENT_MESSAGE_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sent_message_item_view, parent, false);
                return new SentMessageHolder(view, mListener);
            case DATE_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.date_item_view, parent, false);
                return new DateViewHolder(view);
            default:
                throw new IllegalArgumentException("No such input type in RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case RECEIVED_MESSAGE_TYPE:
                ((ReceivedMessageHolder) holder).bind(mContext, mMessages.get(position));
                break;
            case SENT_MESSAGE_TYPE:
                if (mMessages.get(position).isReadReceiver()) {
                ((SentMessageHolder) holder).bind(mContext, mMessages.get(position), R.drawable.ic_done_all_black_24dp);
            } else {
                ((SentMessageHolder) holder).bind(mContext, mMessages.get(position), R.drawable.ic_done_black_24dp);
            }
                break;
            case DATE_TYPE:
                ((DateViewHolder) holder).bind(mMessages.get(position));
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
        UUID myUserId = Preferences.getUserId(mContext.getApplicationContext());
        Message message = mMessages.get(position);

        if (message.getSenderId() == null && message.getReceiverId() == null)
            return DATE_TYPE;

        if (myUserId.equals(message.getSenderId())) {
            return SENT_MESSAGE_TYPE;
        } else {
            return RECEIVED_MESSAGE_TYPE;
        }
    }

    public void addMessages(List<Message> messages) {
        if (messages.size() == 0)
            return;

        int startPosition = mMessages.size();
        mMessages.addAll(messages);

        Calendar day1 = Calendar.getInstance();
        Calendar day2 = Calendar.getInstance();

        int i = startPosition;
        if (i == 0) {
            Message date = new Message();
            date.setDate(mMessages.get(i).getDate());
            mMessages.add(0, date);
            i++;
        }

        while (i < mMessages.size()) {
            day1.setTime(mMessages.get(i - 1).getDate());
            day2.setTime(mMessages.get(i).getDate());
            if (day2.get(Calendar.DAY_OF_MONTH) != day1.get(Calendar.DAY_OF_MONTH) ||
                    day2.get(Calendar.MONTH) != day1.get(Calendar.MONTH) ||
                    day2.get(Calendar.YEAR) != day1.get(Calendar.YEAR)) {
                Message date = new Message();
                date.setDate(day2.getTime());
                mMessages.add(i, date);
                i++;
            }
            i++;
        }


        notifyItemRangeInserted(startPosition, messages.size());
    }

    public void setReceiverMessageIsRead(UUID userId) {
        if (mMessages.size() == 0) {
            return;
        }


        int startPosition = mMessages.size();

        int i = mMessages.size() - 1;

        for (; i >= 0; i--){
            if (getItemViewType(i) == SENT_MESSAGE_TYPE) {
                if (!mMessages.get(i).isReadReceiver() && mMessages.get(i).getReceiverId().equals(userId)) {
                    mMessages.get(i).setReadReceiver(true);
                    startPosition = i;
                } else {
                    break;
                }
            }
        }

        notifyItemRangeChanged(startPosition, mMessages.size());
    }

    public void clear() {
        mMessages.clear();
        notifyDataSetChanged();
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        mListener = listener;
    }

    public interface OnImageClickListener {
        void OnImageClick(Message message);
    }
}