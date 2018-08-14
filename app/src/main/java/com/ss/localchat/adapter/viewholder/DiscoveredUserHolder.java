package com.ss.localchat.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ss.localchat.R;
import com.ss.localchat.adapter.DiscoveredUsersListAdapter;
import com.ss.localchat.model.User;

public class DiscoveredUserHolder extends RecyclerView.ViewHolder{

    private DiscoveredUsersListAdapter.OnItemClickListener mListener;

    private TextView mUserName;
    private TextView mUserId;

    public DiscoveredUserHolder(View itemView, DiscoveredUsersListAdapter.OnItemClickListener listener) {
        super(itemView);

        mListener = listener;

        mUserName = itemView.findViewById(R.id.discovered_user_name_text_view);
        mUserId = itemView.findViewById(R.id.discovered_user_id_text_view);
    }

    public void bind(final User user){
        mUserName.setText(user.getName());
        mUserId.setText(user.getId());

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null){
                    mListener.onClick(user);
                }
            }
        });
    }
}
