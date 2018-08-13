package com.ss.localchat.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ss.localchat.R;
import com.ss.localchat.adapter.viewholder.DiscoveredUserHolder;
import com.ss.localchat.model.Endpoint;

import java.util.ArrayList;
import java.util.List;

public class DiscoveredUsersListAdapter extends RecyclerView.Adapter<DiscoveredUserHolder>{

    private List<Endpoint> mEndpoints;

    public DiscoveredUsersListAdapter() {
        mEndpoints = new ArrayList<>();
    }

    @NonNull
    @Override
    public DiscoveredUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.discovered_user_item_view, parent, false);
        return new DiscoveredUserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscoveredUserHolder holder, int position) {
        Endpoint endpoint = mEndpoints.get(position);
        holder.bind(endpoint);
    }

    @Override
    public int getItemCount() {
        return mEndpoints.size();
    }

    public void addUser(Endpoint endpoint){
        mEndpoints.add(endpoint);
        notifyItemInserted(mEndpoints.size() - 1);
    }

    public void removeUserById(String id){
        for (Endpoint endpoint : mEndpoints){
            if(id.equals(endpoint.getId())){
                mEndpoints.remove(endpoint);
                break;
            }
        }
        notifyDataSetChanged();
    }
}
