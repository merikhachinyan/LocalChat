package com.ss.localchat.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ss.localchat.R;

public class DateViewHolder extends RecyclerView.ViewHolder {

    private TextView mDateTextView;

    public DateViewHolder(View itemView) {
        super(itemView);

        mDateTextView = itemView.findViewById(R.id.date_text_view);
    }

}
