package com.ss.localchat.adapter;

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
import com.ss.localchat.adapter.viewholder.GroupViewHolder;
import com.ss.localchat.db.entity.Chat;
import com.ss.localchat.db.entity.Group;
import com.ss.localchat.db.entity.GroupChat;
import com.ss.localchat.db.entity.Message;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.preferences.Preferences;
import com.ss.localchat.viewmodel.GroupViewModel;
import com.ss.localchat.viewmodel.MessageViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class GroupListAdapter extends RecyclerView.Adapter<GroupViewHolder> {

    private OnItemClickListener mListener;

    private List<GroupChat> mGroupChats;

    private UUID myUserId;

    private List<GroupChat> mFilteredList = new ArrayList<>();
    private MessageViewModel mMessageViewModel;
    private View view;

    public GroupListAdapter(UUID id) {
        myUserId = id;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_view, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        GroupChat groupChat = mFilteredList.get(position);
        holder.bind(groupChat.group, groupChat.message, groupChat.count, mListener, myUserId);
    }

    @Override
    public int getItemCount() {
        return mFilteredList == null ? 0 : mFilteredList.size();
    }

    public void setGroups(List<GroupChat> groups) {
        mGroupChats = groups;
        mFilteredList.clear();
        mFilteredList = groups;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onClick(Group group);

        void onLongClick(Group group, View view);
    }


    private Map<UUID, List<Message>> mUUIDListHashMap = new HashMap<>();

    private ArrayList<GroupChat> filter(List<GroupChat> models, String query) {

        final String lowerCaseQuery = query.toLowerCase();

        final ArrayList<GroupChat> filteredModelList = new ArrayList<>();
        for (final GroupChat model : models) {

            mMessageViewModel = ViewModelProviders.of((FragmentActivity) view.getContext()).get(MessageViewModel.class);

            mMessageViewModel.getMessagesWith(model.group.getId()).observe((FragmentActivity) view.getContext(), new Observer<List<Message>>() {
                @Override
                public void onChanged(@Nullable List<Message> messages) {
                    if (mUUIDListHashMap.get(model.group.getId()) == null) {
                        mUUIDListHashMap.put(model.group.getId(), messages);
                    } else {
                        mUUIDListHashMap.remove(model.group.getId());
                        mUUIDListHashMap.put(model.group.getId(), messages);
                    }

                }
            });
            final String name = model.group.getName().toLowerCase();
            final String message = model.message.getText().toLowerCase();
            if (name.contains(lowerCaseQuery) | message.contains(lowerCaseQuery)) {
                filteredModelList.add(model);
            } else {
                if (mUUIDListHashMap.get(model.group.getId()) != null) {
                    for (int i = 0; i < mUUIDListHashMap.get(model.group.getId()).size(); i++) {
                        String str = mUUIDListHashMap.get(model.group.getId()).get(i).getText().toLowerCase();
                        if (str.contains(lowerCaseQuery)) {
                            boolean duplicate = false;
                            for (int j = 0; j < filteredModelList.size(); j++) {
                                if (filteredModelList.get(j).group.getId() == model.group.getId()) {
                                    duplicate = true;
                                }

                            }
                            if (!duplicate) {
                                filteredModelList.add(model);
                            }
                        }
                    }
                }
            }
        }
        return filteredModelList;
    }

    public void getFilter(String str) {

        mFilteredList = filter(mGroupChats, str);
        notifyDataSetChanged();

    }
}
