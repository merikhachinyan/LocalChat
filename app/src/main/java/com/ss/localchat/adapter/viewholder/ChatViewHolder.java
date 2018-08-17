package com.ss.localchat.adapter.viewholder;

import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.ss.localchat.R;
import com.ss.localchat.adapter.ChatListAdapter;
import com.ss.localchat.db.entity.Message;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.util.CircularTransformation;
import com.ss.localchat.util.DateFormatUtil;


public class ChatViewHolder extends RecyclerView.ViewHolder {

    private ChatListAdapter.OnItemClickListener mListener;

    private ImageView profileImageView;

    private TextView nameTextView;

    private TextView lastMessageTextView;

    private TextView dateTextView;

    private AppCompatTextView unreadMessagesCountTextView;

    public ChatViewHolder(View itemView, ChatListAdapter.OnItemClickListener listener) {

        super(itemView);

        mListener = listener;

        profileImageView = itemView.findViewById(R.id.profile_image_view);
        nameTextView = itemView.findViewById(R.id.name_text_view);
        lastMessageTextView = itemView.findViewById(R.id.last_message_text_view);
        dateTextView = itemView.findViewById(R.id.date_text_view);
        unreadMessagesCountTextView = itemView.findViewById(R.id.unread_messages_count_text_view);
    }

    public void bind(final User user, Message lastMessage, int unreadMessagesCount) {
        nameTextView.setText(user.getName());

        if (lastMessage != null) {
            lastMessageTextView.setVisibility(View.VISIBLE);
            dateTextView.setVisibility(View.VISIBLE);

            lastMessageTextView.setText(lastMessage.getText());
            dateTextView.setText(DateFormatUtil.formatChatDate(lastMessage.getDate()));
        } else {
            lastMessageTextView.setVisibility(View.INVISIBLE);
            dateTextView.setVisibility(View.INVISIBLE);
        }

        if (unreadMessagesCount > 0) {
            unreadMessagesCountTextView.setVisibility(View.VISIBLE);
            unreadMessagesCountTextView.setText(String.valueOf(unreadMessagesCount));
        } else {
            unreadMessagesCountTextView.setVisibility(View.INVISIBLE);
        }


        if (user.getPhotoUrl() == null) {
            Picasso.get()
                    .load(R.drawable.no_user_image)
                    .transform(new CircularTransformation())
                    .into(profileImageView);
        } else {
            Picasso.get()
                    .load(user.getPhotoUrl())
                    .transform(new CircularTransformation())
                    .into(profileImageView);
        }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick(user);
                }
            }
        });
    }
}
