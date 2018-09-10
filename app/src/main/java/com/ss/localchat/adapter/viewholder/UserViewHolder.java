package com.ss.localchat.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ss.localchat.R;
import com.ss.localchat.adapter.UsersListAdapter;
import com.ss.localchat.db.entity.User;

public class UserViewHolder extends RecyclerView.ViewHolder {

    private ImageView profileImageView;

    private TextView nameTextView;

    public UserViewHolder(View itemView) {
        super(itemView);
        profileImageView = itemView.findViewById(R.id.profile_image_view);
        nameTextView = itemView.findViewById(R.id.name_text_view);
    }

    public void bind(final User user, final UsersListAdapter.OnClickListener listener) {
        nameTextView.setText(user.getName());
        Glide.with(itemView)
                .load(user.getPhotoUrl())
                .apply(RequestOptions.circleCropTransform().diskCacheStrategy(DiskCacheStrategy.ALL))
                .error(Glide.with(itemView).load(R.drawable.no_user_image).apply(RequestOptions.circleCropTransform()))
                .into(profileImageView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(user);
                }
            }
        });
    }
}
