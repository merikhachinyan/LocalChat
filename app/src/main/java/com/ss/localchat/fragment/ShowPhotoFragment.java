package com.ss.localchat.fragment;


import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ss.localchat.R;

public class ShowPhotoFragment extends DialogFragment {

    public static final String PHOTO_URI = "photo";


    public ShowPhotoFragment() {
    }

    public static ShowPhotoFragment newInstance(String photoUrl) {
        ShowPhotoFragment fragment = new ShowPhotoFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PHOTO_URI, photoUrl);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.fragment_show_photo);

        ImageView imageView = dialog.findViewById(R.id.photo_show_photo_fragment);

        if (getArguments() != null && getArguments().getString(PHOTO_URI) != null) {
            Uri photoPath = Uri.parse(getArguments().getString(PHOTO_URI));
            Glide.with(getActivity())
                    .load(photoPath)
                    .into(imageView);
        }

        ImageView backImageView = dialog.findViewById(R.id.back_from_show_photo_fragment);
        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        return dialog;
    }
}
