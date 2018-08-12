package com.ss.localchat.fragment;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ss.localchat.R;
import com.ss.localchat.adapter.MessagesRecyclerViewAdapter;
import com.ss.localchat.model.User;
import com.ss.localchat.util.DividerItemDecoration;
import com.ss.localchat.util.Util;

import java.util.ArrayList;
import java.util.List;


public class MessagesFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public MessagesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MessagesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MessagesFragment newInstance(String param1, String param2) {
        MessagesFragment fragment = new MessagesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {


        init(view);
        super.onViewCreated(view, savedInstanceState);
    }

    private RecyclerView mRecyclerView;
    private MessagesRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<User> mUserList;

    public void init(View v) {

        mRecyclerView = v.findViewById(R.id.recyclermessages);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(v.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        User user = new User("rob", "https://techcrunch.com/wp-content/uploads/2018/03/gettyimages-705351545.jpg?w=1390&crop=1");
        mUserList = new ArrayList<>();
        mUserList.add(user);
        mUserList.add(user);
        mUserList.add(user);
        mUserList.add(user);
        mUserList.add(user);
        mUserList.add(user);
        mUserList.add(user);
        // specify an adapter (see also next example)
        mAdapter = new MessagesRecyclerViewAdapter(mUserList);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(v.getContext(), Util.dpToPx(getActivity(), 88));

        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setAdapter(mAdapter);


    }

}
