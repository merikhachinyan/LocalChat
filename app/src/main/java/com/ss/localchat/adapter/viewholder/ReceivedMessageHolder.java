package com.ss.localchat.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ss.localchat.R;
import com.ss.localchat.db.entity.Message;
import com.ss.localchat.util.DateFormatUtil;

public class ReceivedMessageHolder extends RecyclerView.ViewHolder {

    private TextView mSenderName;

    private TextView mMessageText;

    private TextView mMessageDate;


    public ReceivedMessageHolder(View itemView) {
        super(itemView);

        mSenderName = itemView.findViewById(R.id.received_message_sender_name_text_view);
        mMessageText = itemView.findViewById(R.id.received_message_text_view);
        mMessageDate = itemView.findViewById(R.id.received_message_date_text_view);
    }

    public void bind(Message message) {
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
    }
}
