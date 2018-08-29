package com.ss.localchat.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ss.localchat.R;
import com.ss.localchat.db.entity.Message;
import com.ss.localchat.util.DateFormatUtil;

public class SentMessageHolder extends RecyclerView.ViewHolder {

    private TextView mMessageText;
    private TextView mMessageDate;
    private ImageView mReadMessageImage;

    public SentMessageHolder(View itemView) {
        super(itemView);

        mMessageText = itemView.findViewById(R.id.sent_message_text_view);
        mMessageDate = itemView.findViewById(R.id.sent_message_date_text_view);
        mReadMessageImage = itemView.findViewById(R.id.sent_message_unread_image_view);
    }

    public void bind(Message message, int resId) {
        mMessageText.setText(message.getText());
        mMessageDate.setText(DateFormatUtil.formatMessageDate(message.getDate()));
        mReadMessageImage.setImageResource(resId);
    }

    public void bind(Message message) {
        mMessageText.setText(message.getText());
        mMessageDate.setText(DateFormatUtil.formatMessageDate(message.getDate()));
        //mReadMessageImage.setImageResource(resId);
    }
}
