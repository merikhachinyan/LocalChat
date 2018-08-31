package com.ss.localchat.adapter;

import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.ss.localchat.R;
import com.ss.localchat.adapter.viewholder.ChatViewHolder;
import com.ss.localchat.db.entity.Message;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.viewmodel.MessageViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class ChatListAdapter extends RecyclerView.Adapter<ChatViewHolder> implements Filterable {
    private List<User> mFilteredList = new ArrayList<>();
    private OnItemClickListener mListener;

    private List<User> mUsers = new ArrayList<>();

    private HashMap<UUID, Message> mLastMessages = new HashMap<>();

    private HashMap<UUID, Integer> mUnreadMessagesCount = new HashMap<>();

    private MessageViewModel mMessageViewModel;

    public ChatListAdapter(FragmentActivity fragmentActivity) {
        mMessageViewModel = ViewModelProviders.of(fragmentActivity).get(MessageViewModel.class);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_view, parent, false);
        return new ChatViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        User user = mFilteredList.get(position);
        Message lastMessage = mLastMessages.get(user.getId());
        Integer unreadMessagesCount = mUnreadMessagesCount.get(user.getId());

        holder.bind(user, lastMessage, unreadMessagesCount == null ? 0 : unreadMessagesCount);
    }

    @Override
    public int getItemCount() {
        return mFilteredList == null ? 0 : mFilteredList.size();
    }

    public void setUser(User user, Message message, int count) {
        if (!mUsers.contains(user)) {
            mUsers.add(user);
            setLastMessageAndUnreadCount(user, message, count);
            notifyItemInserted(mUsers.size() - 1);
        } else {
            int i = mUsers.indexOf(user);
            mUsers.set(i, user);
            setLastMessageAndUnreadCount(user, message, count);
            notifyItemChanged(i);
        }

        // TODO: 8/27/2018 change users set logic, should pass users list instead of user
        mFilteredList.clear();
        mFilteredList.addAll(mUsers);
    }

    private void setLastMessageAndUnreadCount(User user, Message message, int count) {
        mLastMessages.put(user.getId(), message);
        mUnreadMessagesCount.put(user.getId(), count);
    }



    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String charString = charSequence.toString();

                if (charString.isEmpty()) {

                    mFilteredList = mUsers;
                } else {

                    ArrayList<User> filteredList = new ArrayList<>();

                    for (User androidVersion : mUsers) {

                        if (androidVersion.getName().toLowerCase().contains(charString.toLowerCase()) ) {

                            filteredList.add(androidVersion);
                        }
                    }

                    mFilteredList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mFilteredList.clear();
                mFilteredList.addAll((List<User>) filterResults.values);
                notifyDataSetChanged();
            }
        };
    }

    public interface OnItemClickListener {
        void onClick(User user);
    }
}
