package com.ss.localchat.adapter.viewholder;

import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ss.localchat.R;
import com.ss.localchat.adapter.ChatListAdapter;
import com.ss.localchat.adapter.GroupListAdapter;
import com.ss.localchat.db.entity.Group;
import com.ss.localchat.db.entity.Message;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.preferences.Preferences;
import com.ss.localchat.util.DateFormatUtil;

import java.util.UUID;


public class GroupViewHolder extends RecyclerView.ViewHolder {

    private ImageView groupImageView;

    private TextView nameTextView;

    private TextView senderNameTextView;

    private TextView lastMessageTextView;

    private TextView dateTextView;

    private AppCompatTextView unreadMessagesCountTextView;


    public GroupViewHolder(View itemView) {

        super(itemView);

        groupImageView = itemView.findViewById(R.id.profile_image_view);
        ImageView groupIconImageView = itemView.findViewById(R.id.group_icon_image_view);
        groupIconImageView.setVisibility(View.VISIBLE);
        nameTextView = itemView.findViewById(R.id.name_text_view);
        senderNameTextView = itemView.findViewById(R.id.sender_name_text_view);
        lastMessageTextView = itemView.findViewById(R.id.last_message_text_view);
        dateTextView = itemView.findViewById(R.id.date_text_view);
        unreadMessagesCountTextView = itemView.findViewById(R.id.unread_messages_count_text_view);
    }

    public void bind(final Group group, Message lastMessage, int unreadMessagesCount, final GroupListAdapter.OnItemClickListener listener, UUID id) {
        nameTextView.setText(group.getName());

        if (lastMessage != null) {
            lastMessageTextView.setVisibility(View.VISIBLE);
            dateTextView.setVisibility(View.VISIBLE);
            if (lastMessage.getSenderId().equals(id)) {
                senderNameTextView.setVisibility(View.GONE);
            } else {
                senderNameTextView.setVisibility(View.VISIBLE);
                //TODO lastMessage.getSenderName() is null
                senderNameTextView.setText(lastMessage.getSenderName().concat(":"));
            }
            lastMessageTextView.setText(lastMessage.getText());
            dateTextView.setText(DateFormatUtil.formatChatDate(lastMessage.getDate()));
        } else {
            lastMessageTextView.setVisibility(View.GONE);
            dateTextView.setVisibility(View.GONE);
            senderNameTextView.setVisibility(View.GONE);
        }

        if (unreadMessagesCount > 0) {
            unreadMessagesCountTextView.setVisibility(View.VISIBLE);
            unreadMessagesCountTextView.setText(String.valueOf(unreadMessagesCount));
        } else {
            unreadMessagesCountTextView.setVisibility(View.GONE);
        }

        Glide.with(itemView)
                .load(R.drawable.group_image)
                .apply(RequestOptions.circleCropTransform().diskCacheStrategy(DiskCacheStrategy.ALL))
                .error(Glide.with(itemView).load(R.drawable.no_group_image).apply(RequestOptions.circleCropTransform()))
                .into(groupImageView);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(group);
                }
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onLongClick(group, v);
                }
                return true;
            }
        });
    }
}
