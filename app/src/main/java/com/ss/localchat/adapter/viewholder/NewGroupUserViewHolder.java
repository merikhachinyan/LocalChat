package com.ss.localchat.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ss.localchat.R;
import com.ss.localchat.adapter.NewGroupUsersListAdapter;
import com.ss.localchat.db.entity.User;

public class NewGroupUserViewHolder extends RecyclerView.ViewHolder {

    private ImageView profileImageView;

    private ImageView checkImageView;

    private TextView nameTextView;

    public NewGroupUserViewHolder(View itemView) {
        super(itemView);

        profileImageView = itemView.findViewById(R.id.profile_image_view);
        checkImageView = itemView.findViewById(R.id.check_image_view);
        nameTextView = itemView.findViewById(R.id.name_text_view);
    }

    public void bind(final User user, boolean isChecked, final NewGroupUsersListAdapter.OnClickListener listener) {

        nameTextView.setText(user.getName());

        Glide.with(itemView)
                .load(user.getPhotoUrl())
                .apply(RequestOptions.circleCropTransform().diskCacheStrategy(DiskCacheStrategy.ALL))
                .error(Glide.with(itemView).load(R.drawable.no_user_image).apply(RequestOptions.circleCropTransform()))
                .into(profileImageView);

        if (isChecked) {
            checkImageView.setVisibility(View.VISIBLE);
        } else {
            checkImageView.setVisibility(View.GONE);
        }

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
