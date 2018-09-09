package com.ss.localchat.adapter.viewholder;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.rockerhieu.emojicon.EmojiconTextView;
import com.rockerhieu.emojicon.emoji.Emojicon;
import com.ss.localchat.R;
import com.ss.localchat.adapter.MessageListAdapter;
import com.ss.localchat.db.entity.Message;
import com.ss.localchat.util.DateFormatUtil;

public class ReceivedMessageHolder extends RecyclerView.ViewHolder {

    private TextView mSenderName;

    private EmojiconTextView mMessageText;

    private TextView mMessageDate;

    private ImageView mReceivedPhotoImageView;

    public ReceivedMessageHolder(View itemView) {
        super(itemView);

        mSenderName = itemView.findViewById(R.id.received_message_sender_name_text_view);

        mMessageText = itemView.findViewById(R.id.received_message_text_view);
        mMessageDate = itemView.findViewById(R.id.received_message_date_text_view);
        mReceivedPhotoImageView = itemView.findViewById(R.id.received_message_image_view);
    }

    public void bind(Context context, final Message message, final MessageListAdapter.OnImageClickListener listener) {
        if (message.isGroup()) {
            mSenderName.setText(message.getSenderName());
            mSenderName.setVisibility(View.VISIBLE);
        } else {
            mSenderName.setVisibility(View.GONE);
        }

        if (message.getText() != null) {
            mMessageText.setVisibility(View.VISIBLE);
            mMessageText.setText(message.getText());
        } else {
            mMessageText.setText(View.GONE);
        }
        mMessageDate.setText(DateFormatUtil.formatMessageDate(message.getDate()));

        if (message.getText() != null) {
            mMessageText.setVisibility(View.VISIBLE);
            mMessageText.setText(message.getText());
        } else {
            mMessageText.setVisibility(View.GONE);
        }

        if (message.getPhoto() != null) {
            mReceivedPhotoImageView.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(Uri.parse(message.getPhoto()))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(mReceivedPhotoImageView);
        } else {
            mReceivedPhotoImageView.setVisibility(View.GONE);
        }

        mReceivedPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.OnImageClick(message);
                }
            }
        });
    }
}
