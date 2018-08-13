package com.ss.localchat.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.ss.localchat.R;
import com.ss.localchat.adapter.ChatListAdapter;
import com.ss.localchat.model.User;
import com.ss.localchat.util.CircularTransformation;


public class ChatViewHolder extends RecyclerView.ViewHolder {

    private ChatListAdapter.OnItemClickListener mListener;

    private ImageView imgview;
    private TextView name;
    private TextView message;


    public ChatViewHolder(View itemView, ChatListAdapter.OnItemClickListener listener) {
        super(itemView);

        mListener = listener;

        imgview = itemView.findViewById(R.id.imageView);
        name = itemView.findViewById(R.id.content_messages_name);
        message = itemView.findViewById(R.id.content_messages_message);
    }

    public void bind(final User user) {
        name.setText(user.getName());
        message.setText(user.getMessage());

        if (user.getProfilePhotoUrl() == null) {
            Picasso.get()
                    .load(R.drawable.no_user_image)
                    .transform(new CircularTransformation())
                    .into(imgview);
        } else {
            Picasso.get()
                    .load(user.getProfilePhotoUrl())
                    .transform(new CircularTransformation())
                    .into(imgview);
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

