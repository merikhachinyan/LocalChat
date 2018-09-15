package com.ss.localchat.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ss.localchat.R;
import com.ss.localchat.adapter.DiscoveredUsersListAdapter;
import com.ss.localchat.db.entity.User;

public class DiscoveredUserHolder extends RecyclerView.ViewHolder {

    private DiscoveredUsersListAdapter.OnItemClickListener mListener;

    private TextView mUserName;

    public DiscoveredUserHolder(View itemView, DiscoveredUsersListAdapter.OnItemClickListener listener) {
        super(itemView);

        mListener = listener;

        mUserName = itemView.findViewById(R.id.discovered_user_name_text_view);
    }

    public void bind(final User user) {
        mUserName.setText(user.getName());

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