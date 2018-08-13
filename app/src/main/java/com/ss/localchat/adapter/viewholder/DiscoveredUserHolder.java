package com.ss.localchat.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ss.localchat.R;
import com.ss.localchat.model.Endpoint;

public class DiscoveredUserHolder extends RecyclerView.ViewHolder{

    private TextView mUserName;
    private TextView mUserId;

    public DiscoveredUserHolder(View itemView) {
        super(itemView);

        mUserName = itemView.findViewById(R.id.discovered_user_name_text_view);
        mUserId = itemView.findViewById(R.id.discovered_user_id_text_view);
    }

    public void bind(Endpoint endpoint){
        mUserName.setText(endpoint.getName());
        mUserId.setText(endpoint.getId());
    }
}
