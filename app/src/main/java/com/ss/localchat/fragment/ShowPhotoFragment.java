package com.ss.localchat.fragment;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ss.localchat.R;


public class ShowPhotoFragment extends Fragment {

    public static final String PHOTO_URI = "photo.uri";


    public ShowPhotoFragment() {
    }

    public static ShowPhotoFragment newInstance(String photoUri) {
        ShowPhotoFragment fragment = new ShowPhotoFragment();

        Bundle bundle = new Bundle();
        bundle.putString(PHOTO_URI, photoUri);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_show_photo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        init(view);
    }

    @Override
    public void onResume() {
        super.onResume();

        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onStop() {
        super.onStop();

        Window window = getActivity().getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }

    private void init(View view) {
        ImageView imageView = view.findViewById(R.id.large_image_view_show_photo_fragment);

        if (getArguments() != null) {

            String photoUri = getArguments().getString(PHOTO_URI);

            Glide.with(this)
                    .load(Uri.parse(photoUri))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(imageView);
        }

        ImageView backImage = view.findViewById(R.id.back_from_show_photo_fragment);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
    }
}
