package com.ss.localchat.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ss.localchat.R;
import com.ss.localchat.model.Message;
import com.ss.localchat.util.DateFormatUtil;

public class ReceivedMessageHolder extends RecyclerView.ViewHolder {

    private TextView mMessageText;
    private TextView mMessageDate;

    public ReceivedMessageHolder(View itemView) {
        super(itemView);

        mMessageText = itemView.findViewById(R.id.received_message_text_view);
        mMessageDate = itemView.findViewById(R.id.received_message_date_text_view);
    }

    public void bind(Message message) {
        mMessageText.setText(message.getText());
        mMessageDate.setText(DateFormatUtil.getChatMessageDate(message.getDate()));
    }
}
