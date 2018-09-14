package com.ss.localchat.adapter.viewholder;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.rockerhieu.emojicon.EmojiconTextView;
import com.ss.localchat.R;
import com.ss.localchat.adapter.MessageListAdapter;
import com.ss.localchat.db.entity.Message;
import com.ss.localchat.util.DateFormatUtil;


public class SentMessageHolder extends RecyclerView.ViewHolder {

    private EmojiconTextView mMessageText;
    private TextView mMessageDate;
    private ImageView mReadMessageImage;
    private ImageView mSendPhotoImageView;

    public SentMessageHolder(View itemView) {
        super(itemView);

        mMessageText = itemView.findViewById(R.id.sent_message_text_view);
        mMessageDate = itemView.findViewById(R.id.sent_message_date_text_view);
        mReadMessageImage = itemView.findViewById(R.id.sent_message_unread_image_view);
        mSendPhotoImageView = itemView.findViewById(R.id.sent_message_image_view);
    }

    public void bind(final Message message, final MessageListAdapter.OnImageClickListener listener) {
        mMessageDate.setText(DateFormatUtil.formatMessageDate(message.getDate()));

        if (message.getText() != null) {
            mMessageText.setVisibility(View.VISIBLE);
            mMessageText.setText(message.getText());
        } else {
            mMessageText.setVisibility(View.GONE);
        }

        if (message.getPhoto() != null) {
            mSendPhotoImageView.setVisibility(View.VISIBLE);

            Glide.with(itemView)
                    .load(Uri.parse(message.getPhoto()))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(mSendPhotoImageView);

        } else {
            mSendPhotoImageView.setVisibility(View.GONE);
        }

        mSendPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.OnImageClick(message);
                }
            }
        });

        if (message.isGroup()) {
            mReadMessageImage.setVisibility(View.GONE);
        } else {
            if (message.isReadReceiver()) {
                mReadMessageImage.setImageResource(R.drawable.ic_message_is_read_24dp);
            } else {
                mReadMessageImage.setImageResource(R.drawable.ic_message_is_unread_24dp);
            }
        }
    }
}
