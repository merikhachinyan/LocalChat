package com.ss.localchat.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ss.localchat.R;


public class MessagesViewHolder extends RecyclerView.ViewHolder {
    private ImageView imgview;
    private TextView name;
    private TextView message;


    public MessagesViewHolder(View itemView) {
        super(itemView);


        imgview = (ImageView) itemView.findViewById(R.id.imageView);
        name = (TextView) itemView.findViewById(R.id.content_messages_name);
        message = (TextView) itemView.findViewById(R.id.content_messages_message);

    }

    public ImageView getImgview() {
        return imgview;
    }

    public void setImgview(ImageView imgview) {
        this.imgview = imgview;
    }

    public TextView getName() {
        return name;
    }

    public void setName(TextView name) {
        this.name = name;
    }

    public TextView getMessage() {
        return message;
    }

    public void setMessage(TextView message) {
        this.message = message;
    }
}

